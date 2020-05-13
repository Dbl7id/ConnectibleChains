package com.github.legoatoom.connectiblechains.client.render.entity;

import com.github.legoatoom.connectiblechains.ConnectibleChains;
import com.github.legoatoom.connectiblechains.client.render.entity.model.ChainKnotEntityModel;
import com.github.legoatoom.connectiblechains.enitity.ChainKnotEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.world.LightType;

import javax.sound.midi.Soundbank;
import java.util.*;

/**
 * @see net.minecraft.client.render.entity.LeashKnotEntityRenderer
 * @see net.minecraft.client.render.entity.MobEntityRenderer
 * This class renders the chain you see in game. The block around the fence and the chain.
 * You could use this code to start to understand how this is done.
 * I tried to make it as easy to understand as possible, mainly for myself, since the MobEntityRenderer has a lot of
 * unclear code and shortcuts made.
 */
@Environment(EnvType.CLIENT)
public class ChainKnotEntityRenderer extends EntityRenderer<ChainKnotEntity> {
    private static final Identifier TEXTURE = new Identifier(ConnectibleChains.MODID + ":textures/entity/chain_knot.png");
    private final ChainKnotEntityModel<ChainKnotEntity> model = new ChainKnotEntityModel<>();

    public ChainKnotEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
    }

    public void render(ChainKnotEntity chainKnotEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        super.render(chainKnotEntity, f, g, matrixStack, vertexConsumerProvider, i);
        matrixStack.push();
        matrixStack.scale(-0.9F, -0.9F, 0.9F);
        this.model.setAngles(chainKnotEntity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(this.model.getLayer(TEXTURE));
        this.model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        matrixStack.pop();
        List<Entity> entities = chainKnotEntity.getHoldingEntities();
        for (Entity entity : entities){
            this.method_4073(chainKnotEntity, g, matrixStack, vertexConsumerProvider, entity);
        }
    }

    public Identifier getTexture(ChainKnotEntity chainKnotEntity) {
        return TEXTURE;
    }



    private void method_4073(ChainKnotEntity fromEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, Entity chainOrPlayerEntity) {
        matrixStack.push();
        double d = MathHelper.lerp(f * 0.5F, chainOrPlayerEntity.yaw, chainOrPlayerEntity.prevYaw) * 0.017453292F;
        double e = MathHelper.lerp(f * 0.5F, chainOrPlayerEntity.pitch, chainOrPlayerEntity.prevPitch) * 0.017453292F;
        double g = Math.cos(d);
        double h = Math.sin(d);
        double i = Math.sin(e);
        float t;
        float r;
        float s;
        if (chainOrPlayerEntity instanceof AbstractDecorationEntity) {
            g = 0.0D;
            h = 0.0D;
            double k = MathHelper.lerp(f, chainOrPlayerEntity.prevX, chainOrPlayerEntity.getX());
            double l = MathHelper.lerp(f, chainOrPlayerEntity.prevY, chainOrPlayerEntity.getY());
            double m = MathHelper.lerp(f, chainOrPlayerEntity.prevZ, chainOrPlayerEntity.getZ());
            double o = MathHelper.lerp(f, fromEntity.prevX, fromEntity.getX());
            double p = MathHelper.lerp(f, fromEntity.prevY, fromEntity.getY());
            double q = MathHelper.lerp(f, fromEntity.prevZ, fromEntity.getZ());
            matrixStack.translate(g, 0.3F, h);
            r = (float) (k - o);
            s = (float) (l - p);
            t = (float) (m - q);
        } else {
            double j = Math.cos(e);
            double k = MathHelper.lerp(f, chainOrPlayerEntity.prevX, chainOrPlayerEntity.getX()) - g * 0.7D - h * 0.5D * j;
            double l = MathHelper.lerp(f, chainOrPlayerEntity.prevY + (double) chainOrPlayerEntity.getStandingEyeHeight() * 0.7D, chainOrPlayerEntity.getY() + (double) chainOrPlayerEntity.getStandingEyeHeight() * 0.7D) - i * 0.5D - 0.5D;
            double m = MathHelper.lerp(f, chainOrPlayerEntity.prevZ, chainOrPlayerEntity.getZ()) - h * 0.7D + g * 0.5D * j;
            double o = MathHelper.lerp(f, fromEntity.prevX, fromEntity.getX());
            double p = MathHelper.lerp(f, fromEntity.prevY, fromEntity.getY()) + 0.3F;
            double q = MathHelper.lerp(f, fromEntity.prevZ, fromEntity.getZ());
            matrixStack.translate(0,0.3F, 0);
            r = (float) (k - o);
            s = (float) (l - p);
            t = (float) (m - q);
        }
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getLeash());
        Matrix4f matrix4f = matrixStack.peek().getModel();
        //Create offset based on the location. Example that a line that does not travel in the x then the xOffset will be 0.
        float v = MathHelper.fastInverseSqrt(r * r + t * t) * 0.025F / 2.0F;
        float xOffset = t * v;
        float zOffset = r * v;
        BlockPos zzz = new BlockPos(fromEntity.getCameraPosVec(f));
        int y = this.getBlockLight(fromEntity, zzz);
        int z = chainOrPlayerEntity.isOnFire() ? 15 : chainOrPlayerEntity.world.getLightLevel(LightType.BLOCK, new BlockPos(chainOrPlayerEntity.getCameraPosVec(f)));
        int aa = fromEntity.world.getLightLevel(LightType.SKY, zzz);
        int ab = fromEntity.world.getLightLevel(LightType.SKY, new BlockPos(chainOrPlayerEntity.getCameraPosVec(f)));
        float distance = chainOrPlayerEntity.distanceTo(fromEntity);
        lineBuilder(distance, vertexConsumer, matrix4f, r, s, t, y, z, aa, ab, xOffset, zOffset);
        matrixStack.pop();
    }

    private static float[] rotator(double x, double y, double z){
        double y2 = y*y;
        double x2 = x*x;
        double z2 = z*z;
        double zx = Math.sqrt(x2 + z2);
        double arc1;
        if (zx == 0.0D){ arc1 = Math.acos(0); } else { arc1 = Math.atan(y / zx); }
        double arc2;
        if (z == 0.0D){
            arc2 = Math.acos(0);
        } else {
            arc2 = Math.atan(x/z);
        }
        double d = Math.sin(arc1) * 0.0125F;
        float y_new = (float) (Math.cos(arc1) * 0.0125F);
        float z_new = (float) (Math.cos(arc2) * d);
        float x_new = (float) (Math.sin(arc2) * d);
        float annoying = 0.0F;
        if (zx == 0.0D){
            annoying = 1.0F;
        }
        return new float[]{x_new, y_new, z_new, annoying};
    }

    public static void lineBuilder(float distance, VertexConsumer vertexConsumer, Matrix4f matrix4f, float cordX, float cordY, float cordZ, int i, int j, int k, int l, float xOffset, float zOffset) {
        List<Integer> mPatternA = Arrays.asList(1, 3, 6, 9, 12, 14);
        List<Integer> stbPatternA = Arrays.asList(1, 12);
        List<Integer> ltbPatternA = Collections.singletonList(6);
        List<Integer> stbPatternB = Arrays.asList(0, 14);
        List<Integer> ltbPatternB = Arrays.asList(3, 9);

        int length = (int) Math.floor(distance * 24);
        for (int p = 0; p < length; ++p){
            float s = (float)l / (length - 1);
            int t = (int)MathHelper.lerp(s, (float)i, (float)j);
            int u = (int)MathHelper.lerp(s, (float)k, (float)l);
            int pack = LightmapTextureManager.pack(t, u);
            if(mPatternA.contains(p % 16)){
                middle(length, vertexConsumer, matrix4f, pack, cordX, cordY, cordZ, 0.0125F, length, p, false, xOffset, zOffset, false);
                middle(length, vertexConsumer, matrix4f, pack, cordX, cordY, cordZ, 0.0125F, length, p+1, true, xOffset, zOffset, false);
                middle(length, vertexConsumer, matrix4f, pack, cordX, cordY, cordZ, 0.0125F, length, p, false, xOffset, zOffset, true);
                middle(length, vertexConsumer, matrix4f, pack, cordX, cordY, cordZ, 0.0125F, length, p+1, true, xOffset, zOffset, true);
            }
            if(stbPatternA.contains(p % 16)){
                for (int T = 0; T < 3; T++) {
                    top(length, vertexConsumer, matrix4f, pack, cordX, cordY, cordZ, 0.0125F,  length, p + T, false, xOffset, zOffset, false);
                    top(length, vertexConsumer, matrix4f, pack, cordX, cordY, cordZ, 0.0125F,  length, p + 1 + T, true, xOffset, zOffset, false);
                    bot(length, vertexConsumer, matrix4f, pack, cordX, cordY, cordZ, 0.0125F,  length, p + T, false, xOffset, zOffset, false);
                    bot(length, vertexConsumer, matrix4f, pack, cordX, cordY, cordZ, 0.0125F,  length, p + 1 + T, true, xOffset, zOffset, false);
                }
            }
            if(ltbPatternA.contains(p % 16)){
                for (int T = 0; T < 4; T++) {
                    top(length, vertexConsumer, matrix4f, pack, cordX, cordY, cordZ, 0.0125F,  length, p + T, false, xOffset, zOffset, false);
                    top(length, vertexConsumer, matrix4f, pack, cordX, cordY, cordZ, 0.0125F,  length, p+1 +T, true, xOffset, zOffset, false);
                    bot(length, vertexConsumer, matrix4f, pack, cordX, cordY, cordZ, 0.0125F,  length, p + T, false, xOffset, zOffset, false);
                    bot(length, vertexConsumer, matrix4f, pack, cordX, cordY, cordZ, 0.0125F,  length, p+1 + T, true, xOffset, zOffset, false);
                }
            }
            if(stbPatternB.contains(p % 16)){
                for (int T = 0; T < 2; T++) {
                    top(length, vertexConsumer, matrix4f, pack, cordX, cordY, cordZ, 0.0125F,  length, p + T, false, xOffset, zOffset, true);
                    top(length, vertexConsumer, matrix4f, pack, cordX, cordY, cordZ, 0.0125F,  length, p + 1 + T, true, xOffset, zOffset, true);
                    bot(length, vertexConsumer, matrix4f, pack, cordX, cordY, cordZ, 0.0125F,  length, p + T, false, xOffset, zOffset, true);
                    bot(length, vertexConsumer, matrix4f, pack, cordX, cordY, cordZ, 0.0125F,  length, p + 1 + T, true, xOffset, zOffset, true);
                }
            }
            if(ltbPatternB.contains(p % 16)){
                for (int T = 0; T < 4; T++) {
                    top(length, vertexConsumer, matrix4f, pack, cordX, cordY, cordZ, 0.0125F, length, p + T, false, xOffset, zOffset, true);
                    top(length, vertexConsumer, matrix4f, pack, cordX, cordY, cordZ, 0.0125F, length, p + 1 + T, true, xOffset, zOffset, true);
                    bot(length, vertexConsumer, matrix4f, pack, cordX, cordY, cordZ, 0.0125F, length, p + T, false, xOffset, zOffset, true);
                    bot(length, vertexConsumer, matrix4f, pack, cordX, cordY, cordZ, 0.0125F, length, p + 1 + T, true, xOffset, zOffset, true);
                }
            }
        }
    }

    public static void middle(int V, VertexConsumer vertexConsumer, Matrix4f matrix4f, int i, float cordX, float cordY,
                              float cordZ, float k, int l, int step, boolean bl, float n, float o, boolean shift) {
        double drip = drip(step, V);
        float s = ((float) step / (float) l);
        float[] rotate = rotator(cordX, cordY, cordZ);
        int dirX = 1;
        int dirZ = 1;
        if (cordX <= 0){
            dirX = -1;
        }
        if (cordZ <= 0){
            dirZ = -1;
        }
        float frick = 0.0F;
        if (rotate[3] == 1.0F){
            frick = 0.0125F;
        }
        float t = cordX * s;
        float u = cordY * s;
        float v = cordZ * s;
        float x1 = t + (rotate[0] * dirX) + n;
        float y1 = u + k - rotate[1];
        float z1 = v + (rotate[2] * dirZ) - o;
        float x2 = t - (rotate[0] * dirX) - n;
        float y2 = u + k + rotate[1];
        float z2 = v - (rotate[2] * dirZ) + o;
        float R = 0.12F*0.7F;
        float G = 0.12F*0.7F;
        float B = 0.17F*0.7F;
        if (shift){
            R = 0.12F;
            G = 0.12F;
            B = 0.17F;
             x1 = t + (rotate[0] * dirX) - n + frick;
             z1 = v + (rotate[2] * dirZ) + o - frick;
             x2 = t - (rotate[0] * dirX) + n - frick;
             z2 = v - (rotate[2] * dirZ) - o + frick;
        }
        y1 += drip;
        y2 += drip;
        renderPart(vertexConsumer, matrix4f, i, bl, R, G, B, x1, y1, z1, x2, y2, z2);
    }

    private static void renderPart(VertexConsumer vertexConsumer, Matrix4f matrix4f, int i, boolean bl, float R, float G, float B, float x1, float y1, float z1, float x2, float y2, float z2) {
        if (bl) {
            vertexConsumer.vertex(matrix4f, x1, y1, z1).color(R, G, B, 1.0F).light(i).next();
        }
        vertexConsumer.vertex(matrix4f, x2, y2, z2).color(R, G, B, 1.0F).light(i).next();
        if (!bl) {
            vertexConsumer.vertex(matrix4f, x1, y1, z1).color(R, G, B, 1.0F).light(i).next();
        }
    }

    private static float drip(int x, float V){
        float c = 0.8F;
        float b = -c/V;
        float a = c/(V*V);
        return (a * (x*x) + b*x);
    }

    public static void top(int V, VertexConsumer vertexConsumer, Matrix4f matrix4f, int i, float cordX, float cordY,
                           float cordZ, float k, int l, int step, boolean bl, float n, float o, boolean shift) {
        double drip = drip(step, V);
        float s = ((float) step / (float) l);
        float[] rotate = rotator(cordX, cordY, cordZ);
        int dirX = 1;
        int dirZ = 1;
        if (cordX < 0){
            dirX = -1;
        }
        if (cordZ < 0){
            dirZ = -1;
        }
        float frick = 0.0F;
        if (rotate[3] == 1.0F){
            frick = 0.0125F;
        }
        float t = cordX * s;
        float u = cordY * s;
        float v = cordZ * s;
        float x1 = t - (rotate[0] * dirX) + n;
        float y1 = u + k + rotate[1];
        float z1 = v - (rotate[2] * dirZ) - o;
        float x2 = t - ((rotate[0] * dirX) - n)*3;
        float y2 = u + k + rotate[1]*3;
        float z2 = v - ((rotate[2] * dirZ) + o)*3;
        float R = 0.16F;
        float G = 0.17F;
        float B = 0.21F;
        if (shift){
            R *= 0.8F;
            G *= 0.8F;
            B *= 0.8F;
            x1 = t - (rotate[0] * dirX) - n + frick;
            z1 = v - (rotate[2] * dirZ) + o - frick;
            x2 = t - ((rotate[0] * dirX) + n- frick)*3 ;
            z2 = v - ((rotate[2] * dirZ) - o+ frick)*3 ;
        }
        y1 += drip;
        y2 += drip;
        renderPart(vertexConsumer, matrix4f, i, bl, R, G, B, x1, y1, z1, x2, y2, z2);
    }

    public static void bot(int V, VertexConsumer vertexConsumer, Matrix4f matrix4f, int i, float cordX, float cordY,
                           float cordZ, float k, int l, int step, boolean bl, float n, float o,
                           boolean shift) {
        double drip = drip(step, V);
        float s = ((float) step / (float) l);
        float[] rotate = rotator(cordX, cordY, cordZ);
        int dirX = 1;
        int dirZ = 1;
        if (cordX < 0){
            dirX = -1;
        }
        if (cordZ < 0){
            dirZ = -1;
        }
        float frick = 0.0F;
        if (rotate[3] == 1.0F){
            frick = 0.0125F;
        }
        float t = cordX * s;
        float u = cordY * s;
        float v = cordZ * s;
        float x1 = t + ((rotate[0] * dirX) - n)*3;
        float y1 = u + k - rotate[1]*3;
        float z1 = v + ((rotate[2] * dirZ) + o)*3;
        float x2 = t + (rotate[0] * dirX) - n;
        float y2 = u + k - rotate[1];
        float z2 = v + (rotate[2] * dirZ) + o;
        float R = 0.16F;
        float G = 0.17F;
        float B = 0.21F;
        if (shift){
            R *= 0.8F;
            G *= 0.8F;
            B *= 0.8F;
            x1 = t + ((rotate[0] * dirX) + n - frick)*3;
            z1 = v + ((rotate[2] * dirZ) - o + frick)*3;
            x2 = t + (rotate[0] * dirX) + n - frick;
            z2 = v + (rotate[2] * dirZ) - o + frick;
        }
        y1 += drip;
        y2 += drip;
        renderPart(vertexConsumer, matrix4f, i, bl, R, G, B, x1, y1, z1, x2, y2, z2);
    }

    @Override
    public boolean shouldRender(ChainKnotEntity entity, Frustum frustum, double x, double y, double z) {
        if (super.shouldRender(entity, frustum, x, y, z)){
            return true;
        } else {

            List<Entity> entity1 = entity.getHoldingEntities();
            return entity1.stream().anyMatch(entity2 -> frustum.isVisible(entity2.getVisibilityBoundingBox().expand(7)));
        }
    }

}
