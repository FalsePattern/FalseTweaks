/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2024 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalseTweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FalseTweaks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalseTweaks. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.falsetweaks.modules.voxelizer.loading;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import lombok.val;

import net.minecraft.client.resources.data.BaseMetadataSectionSerializer;
import net.minecraft.util.JsonUtils;

import java.lang.reflect.Type;

public class LayerMetadataSerializer extends BaseMetadataSectionSerializer {
    @Override
    public String getSectionName() {
        return "voxelLayers";
    }

    @Override
    public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        val obj = JsonUtils.getJsonElementAsJsonObject(json, "layer metadata");
        val layers = obj.getAsJsonArray("layers");
        val thicknesses = new float[layers.size()];
        for (int i = 0; i < layers.size(); i++) {
            thicknesses[i] = layers.get(i).getAsFloat();
        }
        return new LayerMetadataSection(thicknesses);
    }
}
