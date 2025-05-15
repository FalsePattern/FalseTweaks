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

package com.falsepattern.falsetweaks.mixin.mixins.client.animfix;

import com.falsepattern.falsetweaks.modules.animfix.AnimationUpdateBatcherRegistry;
import com.falsepattern.falsetweaks.modules.animfix.interfaces.ITextureMapMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.profiler.Profiler;

@Mixin(TextureMap.class)
public abstract class TextureMap_ProfiledMixin implements ITextureMapMixin {
    private static Profiler theProfiler;

    @Inject(method = "updateAnimations",
            at = @At(value = "HEAD"),
            require = 1)
    private void beginBatchAnimations(CallbackInfo ci) {
        if (theProfiler == null) {
            theProfiler = Minecraft.getMinecraft().mcProfiler;
        }
        theProfiler.startSection("updateAnimations");
        AnimationUpdateBatcherRegistry.batcher = getBatcher();
    }

    @Redirect(method = "updateAnimations",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;updateAnimation()V"),
              require = 1)
    private void profileAnimationUpdate(TextureAtlasSprite sprite) {
        theProfiler.startSection(sprite.getIconName());
        sprite.updateAnimation();
        theProfiler.endSection();
    }

    @Inject(method = "updateAnimations",
            at = @At(value = "RETURN"),
            require = 1)
    private void flushBatchAnimations(CallbackInfo ci) {
        AnimationUpdateBatcherRegistry.batcher = null;
        if (getBatcher() != null) {
            theProfiler.startSection("uploadBatch");
            getBatcher().upload();
            theProfiler.endSection();
        }
        theProfiler.endSection();
    }
}
