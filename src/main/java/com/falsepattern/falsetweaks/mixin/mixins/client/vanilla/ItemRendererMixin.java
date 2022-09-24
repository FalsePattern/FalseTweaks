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

package com.falsepattern.falsetweaks.mixin.mixins.client.vanilla;

import com.falsepattern.falsetweaks.renderlists.ItemRenderListManager;
import com.falsepattern.falsetweaks.config.FTConfig;
import lombok.SneakyThrows;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.Tessellator;

@Mixin(net.minecraft.client.renderer.ItemRenderer.class)
public abstract class ItemRendererMixin {
    @SneakyThrows
    @Inject(method = "renderItemIn2D",
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    private static void leFunnyRenderListStart(Tessellator tess, float a, float b, float c, float d, int e, int f, float g, CallbackInfo ci) {
        if (FTConfig.ENABLE_ITEM_RENDERLISTS && ItemRenderListManager.INSTANCE.pre(a, b, c, d, e, f, g)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderItemIn2D",
            at = @At("RETURN"),
            require = 1)
    private static void leFunnyRenderListEnd(Tessellator tess, float a, float b, float c, float d, int e, int f, float g, CallbackInfo ci) {
        if (FTConfig.ENABLE_ITEM_RENDERLISTS) {
            ItemRenderListManager.INSTANCE.post();
        }
    }

    @Redirect(method = "renderItemIn2D",
              slice = @Slice(from = @At(value = "INVOKE",
                                        target = "Lnet/minecraft/client/renderer/Tessellator;draw()I",
                                        ordinal = 0),
                             to = @At(value = "INVOKE",
                                      target = "Lnet/minecraft/client/renderer/Tessellator;startDrawingQuads()V",
                                      ordinal = 5)),
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/Tessellator;draw()I"),
              require = 5)
    private static int batchDrawCalls1(Tessellator instance) {
        return 0;
    }

    @Redirect(method = "renderItemIn2D",
              slice = @Slice(from = @At(value = "INVOKE",
                                        target = "Lnet/minecraft/client/renderer/Tessellator;draw()I",
                                        ordinal = 0),
                             to = @At(value = "INVOKE",
                                      target = "Lnet/minecraft/client/renderer/Tessellator;startDrawingQuads()V",
                                      ordinal = 5)),
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/Tessellator;startDrawingQuads()V"),
              require = 5)
    private static void batchDrawCalls2(Tessellator instance) {

    }
    
    @Inject(method = "renderItemIn2D",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/Tessellator;draw()I",
                       ordinal = 5,
                       shift = At.Shift.BEFORE),
              require = 1)
    private static void plugLeak(Tessellator tess, float u1, float v1, float u2, float v2, int width, int height, float thickness, CallbackInfo ci) {
        if (FTConfig.FIX_ITEM_CRACK) {
            float uOffset = 0.5F * (u1 - u2) / (float) width;
            tess.setNormal(-1.0F, 0.0F, 0.0F);
            for (int k = 0; k < width; ++k) {
                float pos = (float) k / (float) width;
                float u = u1 + (u2 - u1) * pos - uOffset;
                tess.addVertexWithUV(pos + (1f / width), 0, -thickness, u, v2);
                tess.addVertexWithUV(pos, 0, 0, u, v2);
                tess.addVertexWithUV(pos, 1, 0, u, v1);
                tess.addVertexWithUV(pos + (1f / width), 1, -thickness, u, v1);
            }
        }
    }
}
