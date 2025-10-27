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

package com.falsepattern.falsetweaks.mixin.mixins.client.animfix.swansong;

import com.falsepattern.falsetweaks.api.animfix.IAnimationUpdateBatcher;
import com.falsepattern.falsetweaks.modules.animfix.AnimationUpdateBatcherRegistry;
import com.falsepattern.falsetweaks.modules.animfix.interfaces.ITextureMapMixin;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.ventooth.swansong.mixin.interfaces.PBRTextureHolder;
import com.ventooth.swansong.pbr.PBRTextureEngine;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;

@Mixin(value = PBRTextureEngine.class,
       remap = false)
public abstract class PBRTextureEngineMixin {
    @WrapOperation(method = "uploadAtlasSpritesAll",
                   at = @At(value = "INVOKE",
                            target = "Lcom/ventooth/swansong/pbr/PBRTextureEngine;uploadAtlasSpriteLayer(ILit/unimi/dsi/fastutil/objects/ObjectList;IIII)V",
                            ordinal = 0),
                   require = 1)
    private static void uploadNorm(int glName,
                                   ObjectList<TextureAtlasSprite> sprites,
                                   int width,
                                   int height,
                                   int mipLevels,
                                   int defaultCol,
                                   Operation<Void> original,
                                   @Local(argsOnly = true) PBRTextureHolder map) {
        AnimationUpdateBatcherRegistry.batcher = ((ITextureMapMixin)map).ft$getBatcherNorm();
        original.call(glName, sprites, width, height, mipLevels, defaultCol);
        AnimationUpdateBatcherRegistry.batcher = null;
    }

    @WrapOperation(method = "uploadAtlasSpritesAll",
                   at = @At(value = "INVOKE",
                            target = "Lcom/ventooth/swansong/pbr/PBRTextureEngine;uploadAtlasSpriteLayer(ILit/unimi/dsi/fastutil/objects/ObjectList;IIII)V",
                            ordinal = 1),
                   require = 1)
    private static void uploadSpec(int glName,
                                   ObjectList<TextureAtlasSprite> sprites,
                                   int width,
                                   int height,
                                   int mipLevels,
                                   int defaultCol,
                                   Operation<Void> original,
                                   @Local(argsOnly = true) PBRTextureHolder map) {
        AnimationUpdateBatcherRegistry.batcher = ((ITextureMapMixin)map).ft$getBatcherSpec();
        original.call(glName, sprites, width, height, mipLevels, defaultCol);
        AnimationUpdateBatcherRegistry.batcher = null;
    }

    @WrapOperation(method = "uploadAtlasSpritesAll",
                   at = @At(value = "INVOKE",
                            target = "Lcom/ventooth/swansong/pbr/PBRTextureEngine;uploadAtlasSpriteLayer(ILit/unimi/dsi/fastutil/objects/ObjectList;IIII)V",
                            ordinal = 2),
                   require = 1)
    private static void uploadBase(int glName,
                                   ObjectList<TextureAtlasSprite> sprites,
                                   int width,
                                   int height,
                                   int mipLevels,
                                   int defaultCol,
                                   Operation<Void> original,
                                   @Local(argsOnly = true) PBRTextureHolder map) {
        AnimationUpdateBatcherRegistry.batcher = ((ITextureMapMixin)map).getBatcher();
        original.call(glName, sprites, width, height, mipLevels, defaultCol);
        AnimationUpdateBatcherRegistry.batcher = null;
    }

    @Inject(method = "uploadAtlasSpriteLayer",
            at = @At(value = "INVOKE",
                     target = "Ljava/lang/Math;min(II)I"),
            require = 1)
    private static void uploadAnimated(CallbackInfo ci,
                                       @Local TextureAtlasSprite sprite) {
        if (AnimationUpdateBatcherRegistry.batcher != null && sprite.hasAnimationMetadata()) {
            AnimationUpdateBatcherRegistry.batcher.scheduleUpload(sprite.getFrameTextureData(0),
                                                                  sprite.getIconWidth(),
                                                                  sprite.getIconHeight(),
                                                                  sprite.getOriginX(),
                                                                  sprite.getOriginY());
        }
    }

    @WrapOperation(method = "uploadAtlasSpriteLayer",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/texture/TextureUtil;uploadTextureMipmap([[IIIIIZZ)V",
                            remap = true),
                   require = 1)
    private static void dontScheduleNonAnimated(int[][] texture,
                                                int width,
                                                int height,
                                                int xOffset,
                                                int yOffset,
                                                boolean a,
                                                boolean b,
                                                Operation<Void> original) {
        val batch = AnimationUpdateBatcherRegistry.batcher;
        AnimationUpdateBatcherRegistry.batcher = null;
        original.call(texture, width, height, xOffset, yOffset, a, b);
        AnimationUpdateBatcherRegistry.batcher = batch;
    }

    @Inject(method = "updateAnimations",
            at = @At("HEAD"),
            require = 1)
    private static void preUpdateAnimations(CallbackInfo ci,
                                            @Local(argsOnly = true) TextureMap map,
                                            @Share("baseBatcher")LocalRef<IAnimationUpdateBatcher> baseBatcher) {
        baseBatcher.set(AnimationUpdateBatcherRegistry.batcher);
        AnimationUpdateBatcherRegistry.batcher = null;
    }

    @Inject(method = "updateAnimations",
            at = @At(value = "INVOKE",
                     target = "Lcom/ventooth/swansong/sufrace/PBRTexture2D;bind()V",
                     ordinal = 0),
            require = 1)
    private static void preUpdateNorm(CallbackInfo ci,
                                      @Local(argsOnly = true) TextureMap map) {
        AnimationUpdateBatcherRegistry.batcher = ((ITextureMapMixin)map).ft$getBatcherNorm();
    }

    @Inject(method = "updateAnimations",
            at = @At(value = "INVOKE",
                     target = "Lcom/ventooth/swansong/sufrace/PBRTexture2D;bind()V",
                     ordinal = 1),
            require = 1)
    private static void preUpdateSpec(CallbackInfo ci,
                                      @Local(argsOnly = true) TextureMap map) {
        val b = AnimationUpdateBatcherRegistry.batcher;
        if (b != null) {
            b.upload();
        }
        AnimationUpdateBatcherRegistry.batcher = ((ITextureMapMixin)map).ft$getBatcherSpec();
    }

    @Inject(method = "updateAnimations",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/texture/TextureUtil;bindTexture(I)V",
                     remap = true),
            require = 1)
    private static void preUpdateResetTex(CallbackInfo ci) {
        val b = AnimationUpdateBatcherRegistry.batcher;
        if (b != null) {
            b.upload();
        }
    }

    @Inject(method = "updateAnimations",
            at = @At("RETURN"),
            require = 1)
    private static void postUpdateAnimations(CallbackInfo ci,
                                             @Share("baseBatcher")LocalRef<IAnimationUpdateBatcher> baseBatcher) {
        AnimationUpdateBatcherRegistry.batcher = baseBatcher.get();
    }
}
