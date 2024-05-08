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

package com.falsepattern.falsetweaks.mixin.mixins.client.occlusion;

import com.falsepattern.falsetweaks.Compat;
import com.falsepattern.falsetweaks.modules.occlusion.OcclusionHelpers;
import com.falsepattern.falsetweaks.modules.occlusion.OcclusionWorker;
import com.falsepattern.falsetweaks.modules.occlusion.WorldRendererOcclusion;
import com.falsepattern.falsetweaks.modules.occlusion.leakfix.LeakFix;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.shader.TesselatorVertexState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.chunk.EmptyChunk;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.List;

@Mixin(WorldRenderer.class)
@Accessors(fluent = true)
public abstract class WorldRendererMixin implements WorldRendererOcclusion {

    @Shadow
    public World worldObj;

    @Shadow
    public int posX;

    @Shadow
    public int posZ;

    @Shadow
    public List tileEntityRenderers;
    @Shadow
    public boolean needsUpdate;
    @Shadow
    public boolean isInitialized;
    @Shadow
    public TesselatorVertexState vertexState;
    @Shadow
    private List tileEntities;
    @Shadow
    private int bytesDrawn;
    @Shadow
    private int glRenderList;
    private volatile boolean ft$isInUpdateList;
    private volatile boolean ft$nextIsInFrustum;
    private volatile int ft$currentPriority;
    private boolean ft$skipRenderPass;
    private OcclusionWorker.CullInfo ft$cullInfo;
    @Getter
    private boolean hasRenderList;
    @Unique
    private volatile boolean ft$needsSort;
    @Unique
    private volatile boolean ft$needsRebake;

    @Override
    public boolean ft$needsRebake() {
        return ft$needsRebake;
    }

    @Override
    public void ft$needsRebake(boolean value) {
        ft$needsRebake = value;
    }

    @Override
    public boolean ft$skipRenderPass() {
        return ft$skipRenderPass;
    }

    @Override
    public void ft$skipRenderPass(boolean value) {
        ft$skipRenderPass = value;
    }

    private volatile int ft$lastCheckPosX;
    private volatile int ft$lastCheckPosZ;
    private volatile int ft$expectedNeighbors;
    private volatile int ft$currentNeighbors;
    private volatile boolean ft$isNonEmptyChunk;
    private boolean ft$waitingOnShadowOcclusionQuery;
    private boolean ft$isVisibleShadows;

    @Override
    public void ft$updateNeighborCheckState(boolean isNonEmpty, int expected, int current, int posX, int posZ) {
        this.ft$lastCheckPosX = posX;
        this.ft$lastCheckPosZ = posZ;
        this.ft$expectedNeighbors = expected;
        this.ft$currentNeighbors = current;
        this.ft$isNonEmptyChunk = isNonEmpty;
    }

    @Override
    public boolean ft$hasAllNeighbors() {
        return ft$lastCheckPosX == posX && ft$lastCheckPosZ == posZ && ft$expectedNeighbors == ft$currentNeighbors;
    }

    @Override
    public boolean ft$isNonEmptyChunk() {
        return ft$lastCheckPosX == posX && ft$lastCheckPosZ == posZ && ft$isNonEmptyChunk;
    }

    @Override
    public boolean ft$nextIsInFrustum() {
        return ft$nextIsInFrustum;
    }

    @Override
    public void ft$nextIsInFrustum(boolean b) {
        ft$nextIsInFrustum = b;
    }

    @Override
    public int ft$currentPriority() {
        return ft$currentPriority;
    }

    @Override
    public void ft$currentPriority(int i) {
        ft$currentPriority = i;
    }

    @Inject(method = "<init>*",
            at = @At("RETURN"),
            require = 1)
    private void init(CallbackInfo ci) {
        this.ft$cullInfo = new OcclusionWorker.CullInfo();
        ft$waitingOnShadowOcclusionQuery = false;
        ft$isVisibleShadows = true;
    }

    @Inject(method = "markDirty",
            at = @At("TAIL"),
            require = 1)
    private void resetSkipRenderPassFlag(CallbackInfo ci) {
        ft$skipRenderPass = false;
    }

    @Inject(method = "updateRenderer",
            at = @At(value = "FIELD",
                     opcode = Opcodes.PUTSTATIC,
                     target = "Lnet/minecraft/world/chunk/Chunk;isLit:Z",
                     ordinal = 0),
            require = 1,
            cancellable = true)
    private void bailOnEmptyChunk(EntityLivingBase view, CallbackInfo ci) {
        val worldObj = this.worldObj;
        if (worldObj == null) {
            ci.cancel();
            return;
        }
        if (!this.ft$isNonEmptyChunk()) {
            val tileEntityRenderers = this.tileEntityRenderers;
            if (tileEntityRenderers != null && !tileEntityRenderers.isEmpty()) {
                val tileEntities = this.tileEntities;
                if (tileEntities != null) {
                    tileEntities.removeAll(tileEntityRenderers);
                }
                tileEntityRenderers.clear();
            }
            needsUpdate = true;
            isInitialized = false;
            bytesDrawn = 0;
            vertexState = null;
            ci.cancel();
        }
    }

    @Override
    public boolean ft$isInUpdateList() {
        return ft$isInUpdateList;
    }

    @Override
    public void ft$setInUpdateList(boolean b) {
        ft$isInUpdateList = b;
    }

    @Override
    public OcclusionWorker.CullInfo ft$getCullInfo() {
        return ft$cullInfo;
    }

    @Redirect(method = "<init>",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/WorldRenderer;setPosition(III)V"),
              require = 1)
    private void resetRenderListBefore(WorldRenderer thiz, int x, int y, int z) {
        glRenderList = -1;
        hasRenderList = false;
        thiz.setPosition(x, y, z);
    }

    @Inject(method = "setDontDraw",
            at = @At(value = "HEAD"),
            require = 1)
    private void clearLists(CallbackInfo ci) {
        ft$clearList();
    }

    @Override
    public boolean ft$genList() {
        if (hasRenderList) {
            return false;
        }
        glRenderList = LeakFix.allocateWorldRendererBuffer();
        if (glRenderList == -1) {
            return false;
        }
        hasRenderList = true;
        return true;
    }

    @Override
    public boolean ft$hasRenderList() {
        return hasRenderList;
    }

    @Override
    public boolean ft$clearList() {
        if (!hasRenderList) {
            return false;
        }
        hasRenderList = false;
        LeakFix.releaseWorldRendererBuffer(glRenderList);
        glRenderList = -1;
        return true;
    }

    @Inject(method = "preRenderBlocks",
            at = @At("HEAD"),
            require = 1)
    private void prepareRenderList(int p_147890_1_, CallbackInfo ci) {
        if (!Compat.neodymiumActive()) {
            ft$genList();
        }
    }

    @Override
    public boolean ft$needsSort() {
        return ft$needsSort;
    }

    @Override
    public void ft$needsSort(boolean b) {
        ft$needsSort = b;
    }

    @Override
    public boolean ft$waitingOnShadowOcclusionQuery() {
        return ft$waitingOnShadowOcclusionQuery;
    }

    @Override
    public void ft$waitingOnShadowOcclusionQuery(boolean value) {
        ft$waitingOnShadowOcclusionQuery = value;
    }

    @Override
    public boolean ft$isVisibleShadows() {
        return ft$isVisibleShadows;
    }

    @Override
    public void ft$isVisibleShadows(boolean value) {
        ft$isVisibleShadows = value;
    }

    @Unique
    private int ft$frustumCheckCanaryRender;
    @Unique
    private int ft$frustumCheckCanaryShadow;

    @Override
    public void ft$bumpFrustumCheckCanaryRender() {
        ft$frustumCheckCanaryRender++;
    }
    @Override
    public void ft$bumpFrustumCheckCanaryShadow() {
        ft$frustumCheckCanaryShadow++;
    }

    @Override
    public int ft$frustumCheckCanaryRender() {
        return ft$frustumCheckCanaryRender;
    }
    @Override
    public int ft$frustumCheckCanaryShadow() {
        return ft$frustumCheckCanaryShadow;
    }
}
