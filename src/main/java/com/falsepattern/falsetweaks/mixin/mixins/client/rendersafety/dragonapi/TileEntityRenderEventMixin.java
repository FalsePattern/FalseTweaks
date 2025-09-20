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

package com.falsepattern.falsetweaks.mixin.mixins.client.rendersafety.dragonapi;

import Reika.DragonAPI.Instantiable.Event.Client.TileEntityRenderEvent;
import com.falsepattern.falsetweaks.config.RenderingSafetyConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lombok.val;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

@Mixin(value = TileEntityRenderEvent.class,
       remap = false)
public abstract class TileEntityRenderEventMixin {
    @WrapOperation(method = "fire",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/tileentity/TileEntitySpecialRenderer;renderTileEntityAt(Lnet/minecraft/tileentity/TileEntity;DDDF)V"),
                   require = 0)
    private static void wrapTESR(TileEntitySpecialRenderer instance,
                                 TileEntity entity,
                                 double x,
                                 double y,
                                 double z,
                                 float tickDelta,
                                 Operation<Void> original) {
        val enable = RenderingSafetyConfig.ENABLE_TESR;
        if (enable) {
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        }
        original.call(instance, entity, x, y, z, tickDelta);
        if (enable) {
            GL11.glPopAttrib();
        }
    }
}
