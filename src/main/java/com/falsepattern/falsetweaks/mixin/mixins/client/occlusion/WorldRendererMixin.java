/*
 * This file is part of FalseTweaks.
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

import com.falsepattern.falsetweaks.modules.occlusion.leakfix.LeakFix;
import com.falsepattern.falsetweaks.modules.occlusion.leakfix.interfaces.IWorldRendererMixin;
import com.falsepattern.falsetweaks.modules.occlusion.IWorldRenderer;
import com.falsepattern.falsetweaks.modules.occlusion.OcclusionWorker;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.shader.TesselatorVertexState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.chunk.EmptyChunk;

import java.util.List;

@Mixin(WorldRenderer.class)
@Accessors(fluent = true)
public abstract class WorldRendererMixin implements IWorldRenderer, IWorldRendererMixin {
    @Shadow
    public boolean isWaitingOnOcclusionQuery;

    @Shadow
    public World worldObj;

    @Shadow
    public int posX;

    @Shadow
    public int posZ;

    @Shadow
    public List tileEntityRenderers;

    @Shadow
    private List tileEntities;

    @Shadow
    public boolean needsUpdate;

    @Shadow
    public boolean isInitialized;

    @Shadow
    private int bytesDrawn;

    @Shadow
    private TesselatorVertexState vertexState;

    @Shadow private int glRenderList;

    @Shadow public abstract void markDirty();

    private boolean ft$isInUpdateList;
    private boolean ft$isFrustumCheckPending;

    private OcclusionWorker.CullInfo ft$cullInfo;

    @Inject(method = "<init>*",
            at = @At("RETURN"),
            require = 1)
    private void init(CallbackInfo ci) {
        this.ft$cullInfo = new OcclusionWorker.CullInfo();
    }

    @Inject(method = "markDirty",
            at = @At("TAIL"),
            require = 1)
    private void resetOcclusionFlag(CallbackInfo ci) {
        this.isWaitingOnOcclusionQuery = false;
    }

    @Inject(method = "updateRenderer",
            at = @At(value = "FIELD",
                     opcode = Opcodes.PUTSTATIC,
                     target = "Lnet/minecraft/world/chunk/Chunk;isLit:Z",
                     ordinal = 0),
            require = 1,
            cancellable = true)
    private void bailOnEmptyChunk(EntityLivingBase view, CallbackInfo ci) {
        if (worldObj.getChunkFromBlockCoords(posX, posZ) instanceof EmptyChunk) {
            if (tileEntityRenderers.size() > 0) {
                tileEntities.removeAll(tileEntityRenderers);
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

    /**
     * @author FalsePattern
     * @reason No longer used
     */
    @Overwrite
    public void callOcclusionQueryList()
    {

    }

    @Getter
    private boolean hasRenderList;

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
        clearList();
    }

    @Override
    public boolean genList() {
        if (hasRenderList) {
            return false;
        }
        glRenderList = LeakFix.allocateWorldRendererBuffer();
        hasRenderList = true;
        return true;
    }

    @Override
    public boolean clearList() {
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
        genList();
    }
}
