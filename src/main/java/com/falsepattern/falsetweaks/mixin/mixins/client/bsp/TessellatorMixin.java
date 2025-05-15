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
package com.falsepattern.falsetweaks.mixin.mixins.client.bsp;

import com.falsepattern.falsetweaks.Compat;
import com.falsepattern.falsetweaks.modules.bsp.IBSPTessellator;
import com.falsepattern.falsetweaks.modules.triangulator.interfaces.ITriangulatorTessellator;
import com.falsepattern.falsetweaks.modules.triangulator.sorting.BSPTessellatorVertexState;
import com.falsepattern.falsetweaks.modules.triangulator.sorting.ChunkBSPTree;
import lombok.val;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.shader.TesselatorVertexState;

@Mixin(Tessellator.class)
public abstract class TessellatorMixin implements IBSPTessellator {
    @Unique
    private ChunkBSPTree ft$bspTree;

    @Shadow
    private int[] rawBuffer;
    @Shadow
    private int rawBufferIndex;
    @Shadow
    private boolean hasTexture;
    @Shadow
    private boolean hasBrightness;
    @Shadow
    private boolean hasColor;
    @Shadow
    private boolean hasNormals;
    @Shadow
    private double xOffset;
    @Shadow
    private double yOffset;
    @Shadow
    private double zOffset;
    @Shadow
    private int vertexCount;

    @Override
    public TesselatorVertexState ft$getVertexStateBSP(float viewX, float viewY, float viewZ) {
        if (this.rawBufferIndex <= 0) {
            this.ft$bspTree = null;
            return null;
        }
        int[] srcBuf;
        ChunkBSPTree bspTree;
        if (this.ft$bspTree == null) {
            val originalSnapshot = new int[this.rawBufferIndex];
            System.arraycopy(rawBuffer, 0, originalSnapshot, 0, originalSnapshot.length);
            bspTree = new ChunkBSPTree(((ITriangulatorTessellator) this).drawingTris(), Compat.isShaders());
            bspTree.buildTree(originalSnapshot);
            srcBuf = originalSnapshot;
        } else {
            bspTree = this.ft$bspTree;
            srcBuf = bspTree.polygonHolder.getVertexData();
            this.ft$bspTree = null;
        }
        bspTree.traverse(new Vector3f((float) (viewX + xOffset), (float) (viewY + yOffset), (float) (viewZ + zOffset)));
        val stride = bspTree.polygonHolder.vertexStride;
        int[] stateSnapshot = new int[this.rawBufferIndex];
        for (int j = 0, n = bspTree.polygonList.size(); j < n; j++) {
            val i = bspTree.polygonList.get(j);
            System.arraycopy(srcBuf, i * stride, stateSnapshot, j * stride, stride);
        }
        System.arraycopy(stateSnapshot, 0, rawBuffer, 0, stateSnapshot.length);
        return new BSPTessellatorVertexState(stateSnapshot, this.rawBufferIndex, this.vertexCount, this.hasTexture, this.hasBrightness, this.hasNormals, this.hasColor, bspTree);
    }

    @Override
    public void ft$setVertexStateBSP(TesselatorVertexState tvs) {
        if (tvs instanceof BSPTessellatorVertexState) {
            val bsp = (BSPTessellatorVertexState) tvs;
            ft$bspTree = bsp.bspTree;
        } else {
            ft$bspTree = null;
        }
    }

    @Inject(method = "reset",
            at = @At(value = "HEAD"),
            require = 1)
    private void resetState(CallbackInfo ci) {
        ft$bspTree = null;
    }

    @Inject(method = "draw",
            at = @At(value = "HEAD"),
            require = 1)
    private void drawKillBSP(CallbackInfoReturnable<Integer> cir) {
        ft$bspTree = null;
    }
}
