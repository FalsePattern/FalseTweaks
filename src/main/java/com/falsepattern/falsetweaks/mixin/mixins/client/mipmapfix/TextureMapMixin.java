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

package com.falsepattern.falsetweaks.mixin.mixins.client.mipmapfix;

import com.falsepattern.falsetweaks.modules.mipmapfix.MulticoreMipMapEngine;
import lombok.val;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import cpw.mods.fml.common.ProgressManager;

import java.util.Map;

@SuppressWarnings("deprecation")
@Mixin(TextureMap.class)
public abstract class TextureMapMixin {
    @Shadow(remap = false)
    private boolean skipFirst;

    @Shadow
    @Final
    private Map<?, ?> mapRegisteredSprites;

    @Inject(method = "loadTextureAtlas",
            at = @At(value = "INVOKE",
                     target = "Ljava/util/Map;values()Ljava/util/Collection;",
                     ordinal = 0),
            require = 1)
    private void initWorkers(IResourceManager p_110571_1_, CallbackInfo ci) {
        val theBar = ProgressManager.push("Mipmap generation", skipFirst ? 0 : this.mapRegisteredSprites.size());
        MulticoreMipMapEngine.initWorkers(theBar);
    }

    @Redirect(method = "loadTextureAtlas",
              at = @At(value = "INVOKE",
                       target = "Lcpw/mods/fml/common/ProgressManager;push(Ljava/lang/String;I)Lcpw/mods/fml/common/ProgressManager$ProgressBar;",
                       remap = false,
                       ordinal = 1),
              require = 0,
              expect = 0)
    private ProgressManager.ProgressBar noBar(String title, int steps) {
        return null;
    }

    @Redirect(method = "loadTextureAtlas",
              at = @At(value = "INVOKE",
                       target = "Lcpw/mods/fml/common/ProgressManager$ProgressBar;step(Ljava/lang/String;)V",
                       remap = false,
                       ordinal = 1),
              require = 0,
              expect = 0)
    private void noStep(ProgressManager.ProgressBar bar, String message) {

    }

    @Redirect(method = "loadTextureAtlas",
              at = @At(value = "INVOKE",
                       target = "Lcpw/mods/fml/common/ProgressManager;pop(Lcpw/mods/fml/common/ProgressManager$ProgressBar;)V",
                       remap = false,
                       ordinal = 1),
              require = 0,
              expect = 0)
    private void noPop(ProgressManager.ProgressBar newTime) {

    }

    @Redirect(method = "loadTextureAtlas",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;generateMipmaps(I)V",
                       ordinal = 0),
              require = 1)
    private void scheduleToThreads(TextureAtlasSprite instance, int mipMapLevels) {
        MulticoreMipMapEngine.scheduleToThreads(instance, mipMapLevels);
    }

    @Inject(method = "loadTextureAtlas",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;generateMipmaps(I)V",
                     ordinal = 1),
            require = 1)
    private void waitForWorkEnd(IResourceManager p_110571_1_, CallbackInfo ci) {
        MulticoreMipMapEngine.waitForWorkEnd();
    }
}
