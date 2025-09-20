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

package com.falsepattern.falsetweaks.mixin.mixins.client.voxelizer;

import com.falsepattern.falsetweaks.Share;
import com.falsepattern.falsetweaks.config.VoxelizerConfig;
import com.falsepattern.falsetweaks.modules.voxelizer.Layer;
import com.falsepattern.falsetweaks.modules.voxelizer.interfaces.ITextureAtlasSpriteMixin;
import com.falsepattern.falsetweaks.modules.voxelizer.loading.LayerMetadataSection;
import lombok.val;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Mixin(TextureMap.class)
public abstract class TextureMapMixin {
    private static final float BASE_THICKNESS = 0.0625F;
    @Shadow
    @Final
    private Map<String, TextureAtlasSprite> mapRegisteredSprites;

    @Shadow
    protected abstract ResourceLocation completeResourceLocation(ResourceLocation p_147634_1_, int p_147634_2_);

    @Shadow
    public abstract IIcon registerIcon(String p_94245_1_);

    @Inject(method = "loadTextureAtlas",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/texture/TextureMap;registerIcons()V",
                     shift = At.Shift.AFTER),
            require = 1)
    private void loadExtraTexturesForLayers(IResourceManager manager, CallbackInfo ci) {
        val sprites = new HashMap<>(mapRegisteredSprites);
        for (val sprite : sprites.entrySet()) {
            val name = sprite.getKey();
            val iicon = sprite.getValue();
            val tex = (ITextureAtlasSpriteMixin) iicon;
            val loc = new ResourceLocation(name);
            val complete = completeResourceLocation(loc, 0);
            if (iicon.hasCustomLoader(manager, complete)) {
                continue;
            }
            try {
                val resource = manager.getResource(complete);
                val layerMeta = (LayerMetadataSection) resource.getMetadata("voxelLayers");
                if (VoxelizerConfig.VERBOSE_LOG) {
                    Share.log.info("__VOXELIZER__ " + name);
                }
                if (layerMeta != null) {
                    val thicknesses = layerMeta.thicknesses();
                    val layers = new Layer[thicknesses.length];
                    if (VoxelizerConfig.VERBOSE_LOG) {
                        Share.log.info("__VOXELIZER__ FOUND LAYERS!");
                        Share.log.info("__VOXELIZER__ Layer count: " + thicknesses.length);
                    }
                    for (int i = 0; i < thicknesses.length; i++) {
                        layers[thicknesses.length - 1 - i] = new Layer((TextureAtlasSprite) registerIcon(name +
                                                                                                         "_" +
                                                                                                         i),
                                                                       thicknesses[i] * BASE_THICKNESS);
                        registerIcon(name + "_" + i);
                    }
                    tex.layers(layers);
                } else {
                    if (VoxelizerConfig.VERBOSE_LOG) {
                        Share.log.info("__VOXELIZER__ NO LAYERS!");
                    }
                    tex.layers(new Layer(iicon, BASE_THICKNESS));
                }
            } catch (RuntimeException runtimeexception) {
                cpw.mods.fml.client.FMLClientHandler.instance()
                                                    .trackBrokenTexture(complete, runtimeexception.getMessage());
            } catch (IOException ioexception1) {
                cpw.mods.fml.client.FMLClientHandler.instance()
                                                    .trackMissingTexture(complete);
            }
        }
    }
}
