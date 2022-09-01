/*
 * Triangulator
 *
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice, this permission notice and the word "SNEED"
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.triangulator.mixin.mixins.client.optifine.leakfix;

import com.falsepattern.triangulator.leakfix.LeakFix;
import com.falsepattern.triangulator.mixin.helper.IWorldRendererMixin;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.EntityLivingBase;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererVanillaMixin implements IWorldRendererMixin {
    @Shadow
    public abstract void markDirty();

    @Inject(method = "updateRenderer",
            at = @At(value = "FIELD",
                     target = "Lnet/minecraft/client/renderer/WorldRenderer;needsUpdate:Z",
                     opcode = Opcodes.PUTFIELD,
                     shift = At.Shift.AFTER),
            require = 1)
    private void prepareRenderList(EntityLivingBase p_147892_1_, CallbackInfo ci) {
        if (LeakFix.ENABLED) {
            if (genList()) {
                renderAABB();
            }
        }
    }

    @Inject(method = "setPosition",
            at = @At(value = "FIELD",
                     target = "Lnet/minecraft/client/renderer/WorldRenderer;rendererBoundingBox:Lnet/minecraft/util/AxisAlignedBB;",
                     opcode = Opcodes.PUTFIELD,
                     shift = At.Shift.AFTER),
            cancellable = true,
            require = 1)
    private void deferBoundBox(int p_78913_1_, int p_78913_2_, int p_78913_3_, CallbackInfo ci) {
        if (LeakFix.ENABLED) {
            this.markDirty();
            ci.cancel();
        }
    }

    @Inject(method = "callOcclusionQueryList",
            at = @At(value = "HEAD"),
            require = 1)
    private void occlusionQueryEarlyGen(CallbackInfo ci) {
        if (LeakFix.ENABLED) {
            if (genList()) {
                renderAABB();
            }
        }
    }
}
