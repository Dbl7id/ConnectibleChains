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

package com.github.legoatoom.connectiblechains.client.render.entity.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.Entity;

/**
 * Model for the {@link com.github.legoatoom.connectiblechains.enitity.ChainKnotEntity}.
 * Similar to the {@link net.minecraft.client.render.entity.model.LeashKnotEntityModel} code.
 * <p>
 * The model is 6x3x6 pixels big.
 *
 * @author legoatoom
 * @see net.minecraft.client.render.entity.LeashKnotEntityRenderer
 */
@Environment(EnvType.CLIENT)
public class ChainKnotEntityModel<T extends Entity> extends SinglePartEntityModel<T> {
    private final ModelPart chainKnot;

    public ChainKnotEntityModel(ModelPart root) {
        this.chainKnot = root.getChild("knot");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("knot", ModelPartBuilder.create()
                .uv(0, 0)
                .cuboid(-3.0F, -8.0F, -3.0F, 6.0F, 3.0F, 6.0F), ModelTransform.NONE);
        return TexturedModelData.of(modelData, 32, 32);
    }

    @Override
    public ModelPart getPart() {
        return chainKnot;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
    }
}
