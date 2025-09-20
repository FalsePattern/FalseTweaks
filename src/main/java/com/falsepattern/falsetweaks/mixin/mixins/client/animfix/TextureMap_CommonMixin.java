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

import com.falsepattern.falsetweaks.api.animfix.IAnimationUpdateBatcher;
import com.falsepattern.falsetweaks.modules.animfix.AnimationUpdateBatcherRegistry;
import com.falsepattern.falsetweaks.modules.animfix.interfaces.ITextureMapMixin;
import lombok.Getter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;

import java.util.List;

@Mixin(TextureMap.class)
public abstract class TextureMap_CommonMixin implements ITextureMapMixin {
    @Shadow
    private int mipmapLevels;
    @Shadow
    @Final
    private String basePath;
    @Getter
    private IAnimationUpdateBatcher batcher;

    @Inject(method = "loadTexture",
            at = @At(value = "HEAD"),
            require = 1)
    private void setupBatcher(CallbackInfo ci) {
        AnimationUpdateBatcherRegistry.currentAtlas = (TextureMap) (Object) this;
        AnimationUpdateBatcherRegistry.currentName = this.basePath;
    }

    @Inject(method = "loadTexture",
            at = @At(value = "RETURN"),
            require = 1)
    private void finishSetup(CallbackInfo ci) {
        AnimationUpdateBatcherRegistry.currentAtlas = null;
        AnimationUpdateBatcherRegistry.currentName = null;
    }

    @Redirect(method = "loadTextureAtlas",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                       ordinal = 0),
              require = 1)
    private boolean storeAnimatedInBatch(List<TextureAtlasSprite> listAnimatedSprites, Object obj) {
        TextureAtlasSprite sprite = (TextureAtlasSprite) obj;
        listAnimatedSprites.add(sprite);
        AnimationUpdateBatcherRegistry.batcher = batcher;
        TextureUtil.uploadTextureMipmap(sprite.getFrameTextureData(0),
                                        sprite.getIconWidth(),
                                        sprite.getIconHeight(),
                                        sprite.getOriginX(),
                                        sprite.getOriginY(),
                                        false,
                                        false);
        AnimationUpdateBatcherRegistry.batcher = null;
        return true;
    }

    @Override
    public void initializeBatcher(int xOffset, int yOffset, int width, int height) {
        if (batcher != null) {
            batcher.terminate();
        }
        batcher = AnimationUpdateBatcherRegistry.newBatcher(xOffset, yOffset, width, height, mipmapLevels);
    }
}
