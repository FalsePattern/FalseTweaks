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

package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates;

import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadTessellator;
import com.falsepattern.falsetweaks.modules.threadedupdates.saftey.IllegalThreadingDrawing;
import com.falsepattern.falsetweaks.modules.threading.MainThreadContainer;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.shader.TesselatorVertexState;
import cpw.mods.fml.common.Loader;

@Mixin(value = Tessellator.class)
public abstract class TessellatorMixin_Debug {
    @Inject(method = {"setVertexState",
                      "func_154352_a",
                      "setColorRGBA_F",
                      "setColorRGBA",
                      "startDrawing",
                      "addTranslation",
                      "setTranslation",
                      "addVertexWithUV",
                      "setNormal",
                      "setColorOpaque",
                      "addVertex",
                      "setColorOpaque_I",
                      "reset",
                      "setBrightness",
                      "startDrawingQuads",
                      "disableColor",
                      "setColorRGBA_I",
                      "setTextureUV",
                      "setColorOpaque_F"},
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    private void verifyThreadIsCorrect(CallbackInfo ci) {
        if (ft$checkThreadIsInvalid()) {
            ci.cancel();
        }
    }

    @Inject(method = "draw",
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    private void verifyThreadIsCorrectDraw(CallbackInfoReturnable<Integer> cir) {
        if (ft$checkThreadIsInvalid()) {
            cir.setReturnValue(0);
        }
    }

    @Inject(method = "getVertexState",
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    private void verifyThreadIsCorrect(CallbackInfoReturnable<TesselatorVertexState> cir) {
        if (ft$checkThreadIsInvalid()) {
            cir.setReturnValue(null);
        }
    }

    @Unique
    private boolean ft$checkThreadIsInvalid() {
        if (((Object) this) == ThreadTessellator.mainThreadTessellator()) {
            if (!MainThreadContainer.isMainThread()) {
                val modC = Loader.instance()
                                 .activeModContainer();
                IllegalThreadingDrawing.logIllegalMan(modC.getName(), modC.getModId());

                //throw new IllegalStateException("Tried to access main tessellator from non-main thread " + Thread.currentThread().getName());
                return true;
            }
        }

        return false;
    }

}
