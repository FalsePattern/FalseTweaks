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

package com.falsepattern.falsetweaks.mixin.mixins.client.occlusion.optifine;

import com.falsepattern.falsetweaks.modules.occlusion.interfaces.IRenderGlobalMixin;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import stubpackage.WrDisplayListAllocator;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.WorldRenderer;

import java.util.List;

@Mixin(value = RenderGlobal.class,
       priority = -3)
public abstract class RenderGlobalMixin implements IRenderGlobalMixin {

    @Shadow
    public WorldRenderer[] worldRenderers;

    @Shadow
    public int renderChunksWide;

    @Shadow
    public int renderChunksTall;

    @Shadow
    public int renderChunksDeep;

    @Shadow
    public int minBlockX;

    @Shadow
    public int minBlockY;

    @Shadow
    public int minBlockZ;

    @Shadow
    public int maxBlockX;

    @Shadow
    public int maxBlockY;

    @Shadow
    public int maxBlockZ;

    @SuppressWarnings("MissingUnique")
    public List field_72767_j;

    @SuppressWarnings({"MissingUnique", "FieldCanBeLocal", "unused", "AddedMixinMembersNamePattern"})
    private int countSortedWorldRenderers;

    @Override
    public void ft$setSortedRendererCount(int value) {
        countSortedWorldRenderers = value;
    }


    /**
     * Goes through all the renderers setting new positions on them and those that have their position changed are
     * adding to be updated
     *
     * @author _
     * @reason _
     */
    @Overwrite
    public void markRenderersForNewPosition(int posX, int posY, int posZ) {
        posX -= 8;
        posY -= 8;
        posZ -= 8;
        this.minBlockX = Integer.MAX_VALUE;
        this.minBlockY = Integer.MAX_VALUE;
        this.minBlockZ = Integer.MAX_VALUE;
        this.maxBlockX = Integer.MIN_VALUE;
        this.maxBlockY = Integer.MIN_VALUE;
        this.maxBlockZ = Integer.MIN_VALUE;
        int renderBlocksWide = this.renderChunksWide * 16;
        int halfWide = renderBlocksWide / 2;

        for (int chunkW = 0; chunkW < this.renderChunksWide; ++chunkW) {
            int blockW = chunkW * 16;
            int centerW = blockW + halfWide - posX;

            if (centerW < 0) {
                centerW -= renderBlocksWide - 1;
            }

            centerW /= renderBlocksWide;
            blockW -= centerW * renderBlocksWide;

            if (blockW < this.minBlockX) {
                this.minBlockX = blockW;
            }

            if (blockW > this.maxBlockX) {
                this.maxBlockX = blockW;
            }

            for (int chunkD = 0; chunkD < this.renderChunksDeep; ++chunkD) {
                int blockD = chunkD * 16;
                int centerD = blockD + halfWide - posZ;

                if (centerD < 0) {
                    centerD -= renderBlocksWide - 1;
                }

                centerD /= renderBlocksWide;
                blockD -= centerD * renderBlocksWide;

                if (blockD < this.minBlockZ) {
                    this.minBlockZ = blockD;
                }

                if (blockD > this.maxBlockZ) {
                    this.maxBlockZ = blockD;
                }

                for (int chunkT = 0; chunkT < this.renderChunksTall; ++chunkT) {
                    int blockT = chunkT * 16;

                    if (blockT < this.minBlockY) {
                        this.minBlockY = blockT;
                    }

                    if (blockT > this.maxBlockY) {
                        this.maxBlockY = blockT;
                    }

                    WorldRenderer worldrenderer = this.worldRenderers[(chunkD * this.renderChunksTall + chunkT) * this.renderChunksWide + chunkW];
                    boolean flag = worldrenderer.needsUpdate;
                    worldrenderer.setPosition(blockW, blockT, blockD);

                    if (!flag && worldrenderer.needsUpdate) {
                        this.field_72767_j.add(worldrenderer);
                    }
                }
            }
        }
    }

    @Dynamic
    @Redirect(method = "<init>",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/client/renderer/RenderGlobal;displayListAllocator:LWrDisplayListAllocator;",
                       opcode = Opcodes.PUTFIELD,
                       remap = false),
              require = 1)
    private void noAllocator(RenderGlobal rg, WrDisplayListAllocator param) {
    }

    @Dynamic
    @Redirect(method = "deleteAllDisplayLists",
              at = @At(value = "INVOKE",
                       target = "LWrDisplayListAllocator;deleteDisplayLists()V",
                       remap = false),
              require = 1)
    private void noDelete(WrDisplayListAllocator allocator) {
    }
}
