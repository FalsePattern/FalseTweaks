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

package com.falsepattern.falsetweaks.api.dynlights;

import com.falsepattern.lib.StableAPI;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

@StableAPI(since = "__EXPERIMENTAL__")
public interface DynamicLightsDriver {
    @StableAPI.Expose
    DynamicLightsDriver forWorldMesh();
    @StableAPI.Expose
    boolean enabled();
    @StableAPI.Expose
    void entityAdded(Entity entityIn, RenderGlobal renderGlobal);
    @StableAPI.Expose
    void entityRemoved(Entity entityIn, RenderGlobal renderGlobal);
    @StableAPI.Expose
    void update(RenderGlobal renderGlobal);
    @StableAPI.Expose
    int getCombinedLight(int x, int y, int z, int combinedLight);
    @StableAPI.Expose
    int getCombinedLight(Entity entity, int combinedLight);
    @StableAPI.Expose
    void removeLights(RenderGlobal renderGlobal);
    @StableAPI.Expose
    void clear();
    @StableAPI.Expose
    int getCount();
}
