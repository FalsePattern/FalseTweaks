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
package com.falsepattern.falsetweaks.modules.occlusion;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.EntityLivingBase;

import java.util.Comparator;

public class BasicDistanceSorter implements Comparator<WorldRenderer> {
    private final EntityLivingBase renderViewEntity;

    public BasicDistanceSorter(EntityLivingBase renderViewEntity) {
        this.renderViewEntity = renderViewEntity;
    }

    @Override
    public int compare(WorldRenderer wr1, WorldRenderer wr2) {
        return (int) ((wr1.distanceToEntitySquared(renderViewEntity) - wr2.distanceToEntitySquared(renderViewEntity)) * 1024D);
    }
}
