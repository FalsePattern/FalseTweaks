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
package com.falsepattern.falsetweaks.modules.threadedupdates;

import com.falsepattern.falsetweaks.config.ThreadingConfig;
import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadedChunkUpdateHelper.UpdateTask;
import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadedChunkUpdateHelper.UpdateTask.Result;
import lombok.val;
import makamys.neodymium.ducks.NeodymiumTessellator;
import makamys.neodymium.ducks.NeodymiumWorldRenderer;
import makamys.neodymium.renderer.ChunkMesh;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;

public class NeodymiumCompat {
    public static void safeDiscardTask(UpdateTask task) {
        for (val result : task.result) {
            val mesh = getResultMesh(result);
            if (mesh != null) {
                mesh.destroyBuffer();
            }
            setResultMesh(result, null);
        }
    }

    public static void cancelRendering() {
        ChunkMesh.cancelRendering();
    }

    public static void cancelTask(Tessellator tess, WorldRenderer wr) {
        ((NeodymiumTessellator) tess).nd$setCaptureTarget(null);
        val cm = ((NeodymiumWorldRenderer) wr).nd$getChunkMeshes();
        if (cm != null) {
            for (int i = 0; i < 2; i++) {
                val mesh = cm.get(i);
                if (mesh != null) {
                    mesh.destroyBuffer();
                    cm.set(i, null);
                }
            }
        }
    }

    public static void beginCapturing(Tessellator tess, WorldRenderer wr, UpdateTask task, int pass) {
        ((NeodymiumTessellator) tess).nd$captureData();
        ((NeodymiumWorldRenderer) wr).nd$endRenderPass(getResultMesh(task.result[pass]));
    }

    public static void beginRenderPass(UpdateTask task, WorldRenderer wr, int pass) {
        setResultMesh(task.result[pass], ((NeodymiumWorldRenderer) wr).nd$beginRenderPass(pass));
    }

    public static ChunkMesh getResultMesh(Result result) {
        val res = result.resultData;
        return res == null ? null : (ChunkMesh) res;
    }

    public static void setResultMesh(Result result, ChunkMesh mesh) {
        if (ThreadingConfig.EXTRA_DEBUG_INFO) {
            if (result.resultData != null && mesh != null) {
                new Throwable(result.written).printStackTrace();
            }
            if (mesh != null) {
                result.written = new Throwable();
            } else {
                result.written = null;
            }
        }
        result.resultData = mesh;
    }

    public static void beginThreadedPass(WorldRenderer wr, boolean b) {
        //Neodymium can be reset at any point, so we clean up before each task
        cancelRendering();
        ((NeodymiumWorldRenderer) wr).nd$suppressRenderPasses(b);
    }

    public static void beginMainThreadRenderPass(WorldRenderer wr, UpdateTask task, int pass) {
        ((NeodymiumWorldRendererThreadingBridge) wr).ft$ensureNeodymiumChunkMeshesArraylistPresent();
        val meshes = ((NeodymiumWorldRenderer) wr).nd$getChunkMeshes();
        val oldMesh = meshes.get(pass);
        if (oldMesh != null) {
            oldMesh.destroyBuffer();
        }
        meshes.set(pass, getResultMesh(task.result[pass]));
        setResultMesh(task.result[pass], null);
        net.minecraftforge.client.ForgeHooksClient.onPreRenderWorld(wr, pass);
    }

    public static void setSuppressRenderPasses(WorldRenderer wr, boolean state) {
        ((NeodymiumWorldRenderer) wr).nd$suppressRenderPasses(state);
    }
}
