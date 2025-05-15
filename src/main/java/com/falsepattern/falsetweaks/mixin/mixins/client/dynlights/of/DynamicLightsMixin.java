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

package com.falsepattern.falsetweaks.mixin.mixins.client.dynlights.of;

import com.falsepattern.falsetweaks.modules.dynlights.DynamicLightsDrivers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;

@Mixin(targets = "DynamicLights",
       remap = false)
public abstract class DynamicLightsMixin {

    /**
     * @author FalsePattern
     * @reason Integration
     */
    @Overwrite
    public static void entityAdded(Entity entityIn, RenderGlobal renderGlobal) {
        DynamicLightsDrivers.frontend.entityAdded(entityIn, renderGlobal);
    }

    /**
     * @author FalsePattern
     * @reason Integration
     */
    @Overwrite
    public static void entityRemoved(Entity entityIn, RenderGlobal renderGlobal) {
        DynamicLightsDrivers.frontend.entityRemoved(entityIn, renderGlobal);
    }

    /**
     * @author FalsePattern
     * @reason Integration
     */
    @Overwrite
    public static void update(RenderGlobal renderGlobal) {
        DynamicLightsDrivers.frontend.update(renderGlobal);
    }

    /**
     * @author FalsePattern
     * @reason Integration
     */
    @Overwrite
    public static int getCombinedLight(int x, int y, int z, int combinedLight) {
        return DynamicLightsDrivers.frontend.getCombinedLight(x, y, z, combinedLight);
    }

    /**
     * @author FalsePattern
     * @reason Integration
     */
    @Overwrite
    public static int getCombinedLight(Entity entity, int combinedLight) {
        return DynamicLightsDrivers.frontend.getCombinedLight(entity, combinedLight);
    }

    /**
     * @author FalsePattern
     * @reason Integration
     */
    @Overwrite
    public static void removeLights(RenderGlobal renderGlobal) {
        DynamicLightsDrivers.frontend.removeLights(renderGlobal);
    }

    /**
     * @author FalsePattern
     * @reason Integration
     */
    @Overwrite
    public static void clear() {
        DynamicLightsDrivers.frontend.clear();
    }

    /**
     * @author FalsePattern
     * @reason Integration
     */
    @Overwrite
    public static int getCount() {
        return DynamicLightsDrivers.frontend.getCount();
    }
}
