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

package com.falsepattern.falsetweaks.mixin.mixins.client.occlusion.optifastcraft;

import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadSafeSettings;
import lombok.val;
import lombok.var;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntitySorter;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Unique;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;

@Mixin(value = RenderGlobal.class,
       priority = -3)
public abstract class RenderGlobal_ObfMixin {
    @Shadow
    public WorldClient theWorld;

    @Shadow
    public int renderDistanceChunks;

    @Shadow
    public Minecraft mc;

    @Shadow
    public WorldRenderer[] worldRenderers;

    @Shadow
    public int renderChunksWide;

    @Shadow
    public int renderChunksTall;

    @Shadow
    public int renderChunksDeep;

    @Shadow
    public WorldRenderer[] sortedWorldRenderers;

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

    @Shadow
    public List tileEntities;
    @SuppressWarnings("MissingUnique")
    public List field_72767_j;
    @Shadow
    private boolean occlusionEnabled;

    @Shadow
    private int renderEntitiesStartupCounter;

    @Shadow
    private IntBuffer glOcclusionQueryBase;

    @Shadow
    private int glRenderListBase;

    @Shadow
    public abstract void onStaticEntitiesChanged();

    @Shadow
    public abstract void markRenderersForNewPosition(int p_72722_1_, int p_72722_2_, int p_72722_3_);

    /**
     * @author FalsePattern
     * @reason Optifine Unpatch
     */
    @Overwrite
    public void loadRenderers() {
        if (this.theWorld != null) {
            ft$updateLeafFancyGraphics();

            this.renderDistanceChunks = this.mc.gameSettings.renderDistanceChunks;
            int i;

            if (this.worldRenderers != null) {
                for (i = 0; i < this.worldRenderers.length; ++i) {
                    this.worldRenderers[i].stopRendering();
                }
            }

            i = this.renderDistanceChunks * 2 + 1;
            this.renderChunksWide = i;
            this.renderChunksTall = 16;
            this.renderChunksDeep = i;
            this.worldRenderers = new WorldRenderer[this.renderChunksWide * this.renderChunksTall * this.renderChunksDeep];
            this.sortedWorldRenderers = new WorldRenderer[this.renderChunksWide * this.renderChunksTall * this.renderChunksDeep];
            int j = 0;
            int k = 0;
            this.minBlockX = 0;
            this.minBlockY = 0;
            this.minBlockZ = 0;
            this.maxBlockX = this.renderChunksWide;
            this.maxBlockY = this.renderChunksTall;
            this.maxBlockZ = this.renderChunksDeep;
            int l;

            for (l = 0; l < this.field_72767_j.size(); ++l) {
                ((WorldRenderer) this.field_72767_j.get(l)).needsUpdate = false;
            }

            this.field_72767_j.clear();
            this.tileEntities.clear();
            this.onStaticEntitiesChanged();

            for (l = 0; l < this.renderChunksWide; ++l) {
                for (int i1 = 0; i1 < this.renderChunksTall; ++i1) {
                    for (int j1 = 0; j1 < this.renderChunksDeep; ++j1) {
                        this.worldRenderers[(j1 * this.renderChunksTall + i1) * this.renderChunksWide + l] =
                                new WorldRenderer(this.theWorld, this.tileEntities, l * 16, i1 * 16, j1 * 16, this.glRenderListBase + j);

                        if (this.occlusionEnabled) {
                            this.worldRenderers[(j1 * this.renderChunksTall + i1) * this.renderChunksWide + l].glOcclusionQuery = this.glOcclusionQueryBase.get(k);
                        }

                        this.worldRenderers[(j1 * this.renderChunksTall + i1) * this.renderChunksWide + l].isWaitingOnOcclusionQuery = false;
                        this.worldRenderers[(j1 * this.renderChunksTall + i1) * this.renderChunksWide + l].isVisible = true;
                        this.worldRenderers[(j1 * this.renderChunksTall + i1) * this.renderChunksWide + l].isInFrustum = true;
                        this.worldRenderers[(j1 * this.renderChunksTall + i1) * this.renderChunksWide + l].chunkIndex = k++;
                        this.worldRenderers[(j1 * this.renderChunksTall + i1) * this.renderChunksWide + l].markDirty();
                        this.sortedWorldRenderers[(j1 * this.renderChunksTall + i1) * this.renderChunksWide + l] =
                                this.worldRenderers[(j1 * this.renderChunksTall + i1) * this.renderChunksWide + l];
                        this.field_72767_j.add(this.worldRenderers[(j1 * this.renderChunksTall + i1) * this.renderChunksWide + l]);
                        j += 3;
                    }
                }
            }

            if (this.theWorld != null) {
                EntityLivingBase entitylivingbase = this.mc.renderViewEntity;

                if (entitylivingbase != null) {
                    this.markRenderersForNewPosition(MathHelper.floor_double(entitylivingbase.posX), MathHelper.floor_double(entitylivingbase.posY),
                                                     MathHelper.floor_double(entitylivingbase.posZ));
                    Arrays.sort(this.sortedWorldRenderers, new EntitySorter(entitylivingbase));
                }
            }

            this.renderEntitiesStartupCounter = 2;
        }
    }

    @Unique
    private void ft$updateLeafFancyGraphics() {
        val gameSettings = mc.gameSettings;
        var fancyGraphics = false;
        if (ModuleConfig.THREADED_CHUNK_UPDATES()) {
            fancyGraphics = ((ThreadSafeSettings) gameSettings).ft$fancyGraphics();
        } else {
            // This field does not exist if threaded chunk updates are used.
            fancyGraphics = mc.gameSettings.fancyGraphics;
        }
        Blocks.leaves.setGraphicsLevel(fancyGraphics);
        Blocks.leaves2.setGraphicsLevel(fancyGraphics);
    }
}
