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

package com.falsepattern.falsetweaks.modules.beddium;

import com.falsepattern.falsetweaks.api.PassTrackingTessellator;
import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadTessellator;
import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadedUpdateHooks;
import com.falsepattern.falsetweaks.modules.threadedupdates.saftey.ThreadedBlockSafetyRegistry;
import com.ventooth.beddium.api.task.SimpleChunkBuilderMeshingTask;
import com.ventooth.beddium.api.task.WorldRenderRegion;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.val;
import org.embeddedt.embeddium.impl.render.chunk.RenderSection;
import org.embeddedt.embeddium.impl.util.task.CancellationToken;
import org.joml.Vector3d;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.ChunkCache;
import net.minecraftforge.client.ForgeHooksClient;

public abstract class FTChunkBuilderMeshingTask extends SimpleChunkBuilderMeshingTask {
    public FTChunkBuilderMeshingTask(RenderSection render, int time, Vector3d camera) {
        super(render, WorldRenderRegion.forChunkAt(render), time, camera);
    }

    @Override
    protected ChunkCache createChunkCache(WorldRenderRegion worldRenderRegion) {
        return new ChunkCacheBediumDynLights(Minecraft.getMinecraft().theWorld, worldRenderRegion);
    }

    @SuppressWarnings("CastToIncompatibleInterface")
    protected void setTriangulatorRenderPass(int pass) {
        if (ModuleConfig.TRIANGULATOR) {
            ((PassTrackingTessellator) getTessellator()).pass(pass);
        }
    }

    public static final class Basic extends FTChunkBuilderMeshingTask {
        public Basic(RenderSection render, int time, Vector3d camera) {
            super(render, time, camera);
        }

        @Override
        protected Tessellator getTessellator() {
            return ThreadTessellator.mainThreadTessellator();
        }

        @Override
        protected void setRenderPass(int pass) {
            SimpleChunkBuilderMeshingTask.setForgeRenderPass(pass);
            setTriangulatorRenderPass(pass);
        }
    }

    public static final class Threaded extends FTChunkBuilderMeshingTask {
        public Threaded(RenderSection render, int time, Vector3d camera) {
            super(render, time, camera);
        }

        @Override
        protected Tessellator getTessellator() {
            return ThreadTessellator.getThreadTessellator();
        }

        @Override
        protected void setRenderPass(int pass) {
            ThreadedUpdateHooks.setWorldRenderPass(pass);
            setTriangulatorRenderPass(pass);
        }

        @Override
        protected boolean isThreaded() {
            return true;
        }

        @Override
        protected boolean canRenderOffThread(int pass, Block block, int x, int y, int z) {
            val rt = block.getRenderType();
            // Vanilla render types end at 42
            if (rt < 42) {
                return true;
            }
            return ThreadedBlockSafetyRegistry.canBlockRenderOffThread(block);
        }

        @Override
        protected MainThreadWork createMainThreadWork(int pass,
                                                      IntList coords,
                                                      Tessellator tessellator,
                                                      RenderBlocks renderBlocks,
                                                      CancellationToken cancellationToken) {
            return new FTMainThreadWork(pass, coords, cancellationToken, tessellator, renderBlocks);
        }

        protected class FTMainThreadWork extends MainThreadWork {
            public FTMainThreadWork(int pass,
                                    IntList coords,
                                    CancellationToken cancellationToken,
                                    Tessellator tessellator,
                                    RenderBlocks renderBlocks) {
                super(pass, coords, cancellationToken, tessellator, renderBlocks);
            }

            @Override
            public void run() {
                val oldTess = ThreadTessellator.swapMainTessellator(tessellator);
                val oldPass = ForgeHooksClient.getWorldRenderPass();
                SimpleChunkBuilderMeshingTask.setForgeRenderPass(pass);
                try {
                    super.run();
                } finally {
                    SimpleChunkBuilderMeshingTask.setForgeRenderPass(oldPass);
                    val restored = ThreadTessellator.swapMainTessellator(oldTess);
                    assert tessellator == restored;
                }
            }
        }
    }
}
