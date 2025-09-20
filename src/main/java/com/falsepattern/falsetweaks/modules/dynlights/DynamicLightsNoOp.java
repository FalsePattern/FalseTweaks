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

package com.falsepattern.falsetweaks.modules.dynlights;

import com.falsepattern.falsetweaks.api.dynlights.DynamicLightsDriver;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;

public class DynamicLightsNoOp implements DynamicLightsDriver {
    public static final DynamicLightsNoOp INSTANCE = new DynamicLightsNoOp();

    private DynamicLightsNoOp() {
    }

    @Override
    public boolean enabled() {
        return false;
    }

    @Override
    public void entityAdded(Entity entityIn, RenderGlobal renderGlobal) {

    }

    @Override
    public void entityRemoved(Entity entityIn, RenderGlobal renderGlobal) {

    }

    @Override
    public void update(RenderGlobal renderGlobal) {

    }

    @Override
    public int getCombinedLight(int x, int y, int z, int combinedLight) {
        return combinedLight;
    }

    @Override
    public int getCombinedLight(Entity entity, int combinedLight) {
        return combinedLight;
    }

    @Override
    public void removeLights(RenderGlobal renderGlobal) {

    }

    @Override
    public void clear() {

    }

    @Override
    public int getCount() {
        return 0;
    }
}
