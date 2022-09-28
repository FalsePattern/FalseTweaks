/*
 * FalseTweaks
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

package com.falsepattern.falsetweaks.mixin.mixins.client.animfix.minecraft;

import com.falsepattern.falsetweaks.AnimationUpdateBatcher;
import com.falsepattern.falsetweaks.interfaces.ITextureMapMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.profiler.Profiler;

import java.util.List;

@Mixin(TextureMap.class)
public abstract class TextureMapMixin implements ITextureMapMixin {
    private static Profiler theProfiler;
    @Shadow
    private int mipmapLevels;
    @Shadow @Final private String basePath;
    private AnimationUpdateBatcher batcher;

    @Inject(method = "loadTexture",
            at = @At(value = "HEAD"),
            require = 1)
    private void setupBatcher(CallbackInfo ci) {
        AnimationUpdateBatcher.currentAtlas = (TextureMap) (Object) this;
        AnimationUpdateBatcher.currentName = this.basePath;
    }

    @Inject(method = "loadTexture",
            at = @At(value = "RETURN"),
            require = 1)
    private void finishSetup(CallbackInfo ci) {
        AnimationUpdateBatcher.currentAtlas = null;
        AnimationUpdateBatcher.currentName = null;
    }

    @Redirect(method = "loadTextureAtlas",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                       ordinal = 0),
              require = 1)
    private boolean storeAnimatedInBatch(List<TextureAtlasSprite> listAnimatedSprites, Object obj) {
        TextureAtlasSprite sprite = (TextureAtlasSprite) obj;
        listAnimatedSprites.add(sprite);
        AnimationUpdateBatcher.batcher = batcher;
        TextureUtil.uploadTextureMipmap(sprite.getFrameTextureData(0), sprite.getIconWidth(), sprite.getIconHeight(),
                                        sprite.getOriginX(), sprite.getOriginY(), false, false);
        AnimationUpdateBatcher.batcher = null;
        return true;
    }

    @Inject(method = "updateAnimations",
            at = @At(value = "HEAD"),
            require = 1)
    private void beginBatchAnimations(CallbackInfo ci) {
        if (theProfiler == null) {
            theProfiler = Minecraft.getMinecraft().mcProfiler;
        }
        theProfiler.startSection("updateAnimations");
        AnimationUpdateBatcher.batcher = batcher;
    }

    @Inject(method = "updateAnimations",
            at = @At(value = "RETURN"),
            require = 1)
    private void flushBatchAnimations(CallbackInfo ci) {
        AnimationUpdateBatcher.batcher = null;
        if (batcher != null) {
            theProfiler.startSection("uploadBatch");
            batcher.upload();
            theProfiler.endSection();
        }
        theProfiler.endSection();
    }

    @Override
    public void initializeBatcher(int xOffset, int yOffset, int width, int height) {
        if (batcher != null) {
            batcher.terminate();
        }
        batcher = new AnimationUpdateBatcher(xOffset, yOffset, width, height, mipmapLevels);
    }
}
