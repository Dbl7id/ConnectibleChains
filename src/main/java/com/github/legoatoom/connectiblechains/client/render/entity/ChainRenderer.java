/*
 * Copyright (C) 2022 legoatoom
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.legoatoom.connectiblechains.client.render.entity;

import com.github.legoatoom.connectiblechains.ConnectibleChains;
import com.github.legoatoom.connectiblechains.chain.ChainType;
import com.github.legoatoom.connectiblechains.chain.UV;
import com.github.legoatoom.connectiblechains.util.Helper;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import static com.github.legoatoom.connectiblechains.util.Helper.drip2;
import static com.github.legoatoom.connectiblechains.util.Helper.drip2prime;

public class ChainRenderer {
    private static final float CHAIN_SCALE = 1f;
    private static final int MAX_SEGMENTS = 2048;
    private final Object2ObjectOpenHashMap<BakeKey, ChainModel> models = new Object2ObjectOpenHashMap<>(256);

    public void renderBaked(VertexConsumer buffer, MatrixStack matrices, BakeKey key, Vec3f chainVec, ChainType chainType, int blockLight0, int blockLight1, int skyLight0, int skyLight1) {
        ChainModel model;
        if (models.containsKey(key)) {
            model = models.get(key);
        } else {
            model = buildModel(chainVec, chainType);
            models.put(key, model);
        }
        model.render(buffer, matrices, blockLight0, blockLight1, skyLight0, skyLight1);
    }

    public void render(VertexConsumer buffer, MatrixStack matrices, Vec3f chainVec, ChainType chainType, int blockLight0, int blockLight1, int skyLight0, int skyLight1) {
        ChainModel model = buildModel(chainVec, chainType);
        model.render(buffer, matrices, blockLight0, blockLight1, skyLight0, skyLight1);
    }

    private ChainModel buildModel(Vec3f chainVec, ChainType chainType) {
        float desiredSegmentLength = 1f / ConnectibleChains.runtimeConfig.getQuality();
        int initialCapacity = (int) (2f * Helper.lengthOf(chainVec) / desiredSegmentLength);
        ChainModel.Builder builder = ChainModel.builder(initialCapacity);

        if (chainVec.getX() == 0 && chainVec.getZ() == 0) {
            buildFaceVertical(builder, chainVec, 45, chainType.uvSIdeA());
            buildFaceVertical(builder, chainVec, -45, chainType.uvSideB());
        } else {
            buildFace(builder, chainVec, 45, chainType.uvSIdeA());
            buildFace(builder, chainVec, -45, chainType.uvSideB());
        }

        return builder.build();
    }

    private void buildFaceVertical(ChainModel.Builder builder, Vec3f v, float angle, UV uv) {
        float actualSegmentLength = 1f / ConnectibleChains.runtimeConfig.getQuality();
        float chainWidth = (uv.x1() - uv.x0()) / 16 * CHAIN_SCALE;

        Vec3f normal = new Vec3f((float) Math.cos(Math.toRadians(angle)), 0, (float) Math.sin(Math.toRadians(angle)));
        normal.scale(chainWidth);

        Vec3f vert00 = new Vec3f(-normal.getX() / 2, 0, -normal.getZ() / 2), vert01 = vert00.copy();
        vert01.add(normal);
        Vec3f vert10 = new Vec3f(-normal.getX() / 2, 0, -normal.getZ() / 2), vert11 = vert10.copy();
        vert11.add(normal);

        float uvv0 = 0, uvv1 = 0;
        boolean lastIter_ = false;
        for (int segment = 0; segment < MAX_SEGMENTS; segment++) {
            if (vert00.getY() + actualSegmentLength >= v.getY()) {
                lastIter_ = true;
                actualSegmentLength = v.getY() - vert00.getY();
            }

            vert10.add(0, actualSegmentLength, 0);
            vert11.add(0, actualSegmentLength, 0);

            uvv1 += actualSegmentLength / CHAIN_SCALE;

            builder.vertex(vert00).uv(uv.x0() / 16f, uvv0).next();
            builder.vertex(vert01).uv(uv.x1() / 16f, uvv0).next();
            builder.vertex(vert11).uv(uv.x1() / 16f, uvv1).next();
            builder.vertex(vert10).uv(uv.x0() / 16f, uvv1).next();

            if (lastIter_) break;

            uvv0 = uvv1;

            vert00.set(vert10);
            vert01.set(vert11);
        }
    }

    private void buildFace(ChainModel.Builder builder, Vec3f v, float angle, UV uv) {
        float actualSegmentLength, desiredSegmentLength = 1f / ConnectibleChains.runtimeConfig.getQuality();
        float distance = Helper.lengthOf(v), distanceXZ = (float) Math.sqrt(v.getX() * v.getX() + v.getZ() * v.getZ());
        // Original code used total distance between start and end instead of horizontal distance
        // That changed the look of chains when there was a big height difference, but it looks better.
        float wrongDistanceFactor = distance / distanceXZ;

        Vec3f vert00 = new Vec3f(), vert01 = new Vec3f(), vert11 = new Vec3f(), vert10 = new Vec3f();
        Vec3f normal = new Vec3f(), rotAxis = new Vec3f();

        float chainWidth = (uv.x1() - uv.x0()) / 16 * CHAIN_SCALE;
        float uvv0, uvv1 = 0, gradient, x, y;
        Vec3f point0 = new Vec3f(), point1 = new Vec3f();
        Quaternion rotator;

        // All of this setup can probably go, but I can't figure out
        // how to integrate it into the loop :shrug:
        point0.set(0, (float) drip2(0, distance, v.getY()), 0);
        gradient = (float) drip2prime(0, distance, v.getY());
        normal.set(-gradient, Math.abs(distanceXZ / distance), 0);
        normal.normalize();

        x = estimateDeltaX(desiredSegmentLength, gradient);
        gradient = (float) drip2prime(x * wrongDistanceFactor, distance, v.getY());
        y = (float) drip2(x * wrongDistanceFactor, distance, v.getY());
        point1.set(x, y, 0);

        rotAxis.set(point1.getX() - point0.getX(), point1.getY() - point0.getY(), point1.getZ() - point0.getZ());
        rotAxis.normalize();
        rotator = rotAxis.getDegreesQuaternion(angle);

        normal.rotate(rotator);
        normal.scale(chainWidth);
        vert10.set(point0.getX() - normal.getX() / 2, point0.getY() - normal.getY() / 2, point0.getZ() - normal.getZ() / 2);
        vert11.set(vert10);
        vert11.add(normal);

        actualSegmentLength = Helper.distanceBetween(point0, point1);

        boolean lastIter_ = false;
        for (int segment = 0; segment < MAX_SEGMENTS; segment++) {
            rotAxis.set(point1.getX() - point0.getX(), point1.getY() - point0.getY(), point1.getZ() - point0.getZ());
            rotAxis.normalize();
            rotator = rotAxis.getDegreesQuaternion(angle);

            // This normal is orthogonal to the face normal
            normal.set(-gradient, Math.abs(distanceXZ / distance), 0);
            normal.normalize();
            normal.rotate(rotator);
            normal.scale(chainWidth);

            vert00.set(vert10);
            vert01.set(vert11);

            vert10.set(point1.getX() - normal.getX() / 2, point1.getY() - normal.getY() / 2, point1.getZ() - normal.getZ() / 2);
            vert11.set(vert10);
            vert11.add(normal);

            uvv0 = uvv1;
            uvv1 = uvv0 + actualSegmentLength / CHAIN_SCALE;

            builder.vertex(vert00).uv(uv.x0() / 16f, uvv0).next();
            builder.vertex(vert01).uv(uv.x1() / 16f, uvv0).next();
            builder.vertex(vert11).uv(uv.x1() / 16f, uvv1).next();
            builder.vertex(vert10).uv(uv.x0() / 16f, uvv1).next();

            if (lastIter_) break;

            point0.set(point1);

            x += estimateDeltaX(desiredSegmentLength, gradient);
            if (x >= distanceXZ) {
                lastIter_ = true;
                x = distanceXZ;
            }

            gradient = (float) drip2prime(x * wrongDistanceFactor, distance, v.getY());
            y = (float) drip2(x * wrongDistanceFactor, distance, v.getY());
            point1.set(x, y, 0);

            actualSegmentLength = Helper.distanceBetween(point0, point1);
        }
    }

    /**
     * Estimate Δx based on current gradient to get segments with equal length
     * k ... Gradient
     * T ... Tangent
     * s ... Segment Length
     * <p>
     * T = (1, k)
     * <p>
     * Δx = (s * T / |T|).x
     * Δx = s * T.x / |T|
     * Δx = s * 1 / |T|
     * Δx = s / |T|
     * Δx = s / √(1^2 + k^2)
     * Δx = s / √(1 + k^2)
     *
     * @param s the desired segment length
     * @param k the gradient
     * @return Δx
     */
    private float estimateDeltaX(float s, float k) {
        return (float) (s / Math.sqrt(1 + k * k));
    }

    public void purge() {
        models.clear();
    }

    public static class BakeKey {
        private final int hash;

        public BakeKey(Vec3d srcPos, Vec3d dstPos, ChainType chainType) {
            float dY = (float) (srcPos.y - dstPos.y);
            float dXZ = Helper.distanceBetween(
                    new Vec3f((float) srcPos.x, 0, (float) srcPos.z),
                    new Vec3f((float) dstPos.x, 0, (float) dstPos.z));

            int hash = Float.floatToIntBits(dY);
            hash = 31 * hash + Float.floatToIntBits(dXZ);
            hash = 31 * hash + chainType.uvSIdeA().hashCode();
            hash = 31 * hash + chainType.uvSideB().hashCode();
            this.hash = hash;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BakeKey bakeKey = (BakeKey) o;
            return hash == bakeKey.hash;
        }
    }
}
