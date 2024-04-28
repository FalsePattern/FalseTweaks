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

package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates;

import com.falsepattern.falsetweaks.modules.threadedupdates.IllegalThreadingDrawing;
import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadedChunkUpdateHelper;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.Tessellator;
import cpw.mods.fml.common.Loader;

@Mixin(value = Tessellator.class)
public abstract class TessellatorMixin_DebugFast {
    @Inject(method = {"setVertexState", "func_154352_a", "setColorRGBA_F", "setColorRGBA", "startDrawing", "addTranslation", "setTranslation", "addVertexWithUV", "setNormal",
                      "setColorOpaque", "addVertex", "setColorOpaque_I", "reset", "setBrightness", "startDrawingQuads", "disableColor", "setColorRGBA_I", "setTextureUV",
                      "setColorOpaque_F"},
            at = @At("HEAD"),
            require = 1)
    private void verifyThreadIsCorrect(CallbackInfo ci) {
        ft$verifyThread();
    }

    @Inject(method = "draw",
            at = @At("HEAD"),
            require = 1)
    private void verifyThreadIsCorrectDraw(CallbackInfoReturnable<Integer> cir) {
        ft$verifyThread();
    }

    @Inject(method = "getVertexState",
            at = @At("HEAD"),
            require = 1)
    private void verifyThreadIsCorrect(CallbackInfoReturnable<Integer> cir) {
        ft$verifyThread();
    }

    @Unique
    private void ft$verifyThread() {
        if (((Object) this) == Tessellator.instance) {
            if (!ThreadedChunkUpdateHelper.isMainThread()) {
                val modC = Loader.instance().activeModContainer();
                IllegalThreadingDrawing.logIllegalMan(modC.getName(), modC.getModId());

                throw new IllegalStateException("Tried to access main tessellator from non-main thread " + Thread.currentThread().getName());
            }
        }

    }

}
