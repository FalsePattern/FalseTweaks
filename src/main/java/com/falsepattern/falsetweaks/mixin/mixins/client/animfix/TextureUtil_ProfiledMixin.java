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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.profiler.Profiler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mixin(TextureUtil.class)
@SideOnly(Side.CLIENT)
public abstract class TextureUtil_ProfiledMixin {
    private static Profiler theProfiler;

    @Inject(method = "uploadTextureMipmap",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private static void uploadTextureBatchable(int[][] texture,
                                               int width,
                                               int height,
                                               int xOffset,
                                               int yOffset,
                                               boolean ignored1,
                                               boolean ignored2,
                                               CallbackInfo ci) {
        if (theProfiler == null) {
            theProfiler = Minecraft.getMinecraft().mcProfiler;
        }
        if (AnimationUpdateBatcherRegistry.batcher != null) {
            theProfiler.startSection("copyToBatch");
            boolean ended = AnimationUpdateBatcherRegistry.batcher.scheduleUpload(texture,
                                                                                  width,
                                                                                  height,
                                                                                  xOffset,
                                                                                  yOffset);
            theProfiler.endSection();
            if (ended) {
                ci.cancel();
                return;
            }
        }
        theProfiler.startSection("uploadUnbatched");
    }

    @Inject(method = "uploadTextureMipmap",
            at = @At(value = "RETURN"),
            require = 1)
    private static void uploadUnbatchedEnd(CallbackInfo ci) {
        theProfiler.endSection();
    }
}
