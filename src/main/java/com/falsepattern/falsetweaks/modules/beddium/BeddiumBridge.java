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

import com.falsepattern.falsetweaks.config.ThreadingConfig;
import com.ventooth.beddium.api.task.ChunkTaskProvider;
import com.ventooth.beddium.api.task.ChunkTaskRegistry;
import org.embeddedt.embeddium.impl.render.chunk.RenderSection;
import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildOutput;
import org.embeddedt.embeddium.impl.render.chunk.compile.tasks.ChunkBuilderTask;
import org.joml.Vector3d;

@SuppressWarnings("unused") // Called via multi-release logic
public class BeddiumBridge {
    private static final boolean ALWAYS_DEFER_CHUNK_UPDATES = false; // TODO: Wire up to a config.

    public static void registerBasicProvider() {
        ChunkTaskRegistry.registerProvider(new ChunkTaskProvider() {
            @Override
            public ChunkBuilderTask<ChunkBuildOutput> createRebuildTask(RenderSection render,
                                                                        int time,
                                                                        Vector3d camera) {
                return new FTChunkBuilderMeshingTask.Basic(render, time, camera);
            }

            @Override
            public int threadCount() {
                return -1;
            }

            @Override
            public boolean alwaysDeferChunkUpdates() {
                return false;
            }
        }, 1000);
    }

    public static void registerThreadingProvider() {
        ChunkTaskRegistry.registerProvider(new ChunkTaskProvider() {
            @Override
            public ChunkBuilderTask<ChunkBuildOutput> createRebuildTask(RenderSection render,
                                                                        int time,
                                                                        Vector3d camera) {
                return new FTChunkBuilderMeshingTask.Threaded(render, time, camera);
            }

            @Override
            public int threadCount() {
                return ThreadingConfig.CHUNK_UPDATE_THREADS;
            }

            @Override
            public boolean alwaysDeferChunkUpdates() {
                return ALWAYS_DEFER_CHUNK_UPDATES;
            }
        }, 1000);
    }
}
