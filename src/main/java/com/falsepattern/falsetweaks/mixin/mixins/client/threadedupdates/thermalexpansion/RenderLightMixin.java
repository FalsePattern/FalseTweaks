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

package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates.thermalexpansion;

import cofh.thermalexpansion.render.RenderLight;
import com.falsepattern.falsetweaks.modules.threadedupdates.interop.ThermalExpansionCompat;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RenderLight.class,
       remap = false)
public abstract class RenderLightMixin {
    @Redirect(method = "renderWorldBlock",
              at = @At(value = "FIELD",
                       opcode = Opcodes.GETSTATIC,
                       target = "Lcofh/core/block/BlockCoFHBase;renderPass:I"),
              require = 1)
    private int redirectGetRenderPass() {
        return ThermalExpansionCompat.getCofhBlockRenderPass();
    }
}
