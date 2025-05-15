/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2025 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalseTweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
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

import lombok.RequiredArgsConstructor;
import lombok.val;

import net.minecraft.client.renderer.WorldRenderer;

import java.util.Comparator;

@RequiredArgsConstructor
public class WRComparator implements Comparator<WorldRenderer> {
    public final boolean withPrio;
    public float posX;
    public float posY;
    public float posZ;

    public float distanceToEntitySquared(WorldRenderer rend) {
        float x = posX - rend.posXPlus;
        float y = (posY - rend.posYPlus) * 3;
        float z = posZ - rend.posZPlus;
        return x * x + y * y + z * z;
    }

    @Override
    public int compare(WorldRenderer o1, WorldRenderer o2) {
        if (withPrio) {
            val prio1 = ((WorldRendererOcclusion) o1).ft$currentPriority();
            val prio2 = ((WorldRendererOcclusion) o2).ft$currentPriority();
            val pComp = Integer.compare(prio1, prio2);
            if (pComp != 0) {
                return pComp;
            }
        }
        val d1 = distanceToEntitySquared(o1);
        val d2 = distanceToEntitySquared(o2);
        return Float.compare(d1, d2);
    }
}
