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
import com.falsepattern.falsetweaks.modules.occlusion.OcclusionRenderer;
import com.falsepattern.falsetweaks.modules.occlusion.OcclusionWorker;
import com.falsepattern.falsetweaks.modules.occlusion.interfaces.IRenderGlobalMixin;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import java.nio.IntBuffer;
import java.util.Comparator;
import java.util.List;

@Mixin(value = RenderGlobal.class,
       priority = -2)
public abstract class RenderGlobalMixin implements IRenderGlobalMixin {
    /**
     * Queue a renderer to be updated.
     */
    @Inject(method = "markBlocksForUpdate",
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    private void handleOffthreadUpdate(int x1, int y1, int z1, int x2, int y2, int z2, CallbackInfo ci) {
        ci.cancel();
        OcclusionHelpers.renderer.handleOffthreadUpdate(x1, y1, z1, x2, y2, z2);
    }

    @Redirect(method = "renderEntities",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/entity/RenderManager;renderEntitySimple(Lnet/minecraft/entity/Entity;F)Z"),
              require = 1)
    private boolean skipRenderingIfNotVisible(RenderManager instance, Entity entity, float tick) {
        return OcclusionHelpers.renderer.skipRenderingIfNotVisible(instance, entity, tick);
    }

    /**
     * @author skyboy, embeddedt
     * @reason Include information on occlusion
     */
    @Overwrite
    public String getDebugInfoRenders() {
        return OcclusionHelpers.renderer.getDebugInfoRenders();
    }

    @Inject(method = "<init>",
            at = @At("RETURN"),
            require = 1)
    private void initBetterLists(Minecraft p_i1249_1_, CallbackInfo ci) {
        OcclusionHelpers.renderer = new OcclusionRenderer((RenderGlobal) (Object) this);
        OcclusionHelpers.renderer.initBetterLists();
    }

    @Redirect(method = "loadRenderers",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/List;clear()V",
                       ordinal = 0),
              require = 1)
    private void clearRendererUpdateQueue(List instance) {
        OcclusionHelpers.renderer.clearRendererUpdateQueue(instance);
    }

    @Redirect(method = "loadRenderers",
              at = @At(value = "INVOKE",
                       target = "LWrUpdates;makeWorldRenderer(Lnet/minecraft/world/World;Ljava/util/List;IIII)Lnet/minecraft/client/renderer/WorldRenderer;",
                       remap = false),
              expect = 0)
    @Dynamic
    private WorldRenderer optifineMakeWorldRenderer(World worldObj, List tileEntities, int x, int y, int z, int glRenderListBase) {
        return new WorldRenderer(worldObj, tileEntities, x, y, z, glRenderListBase);
    }

    @Redirect(method = {"loadRenderers", "markRenderersForNewPosition"},
              at = @At(value = "INVOKE",
                       target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                       ordinal = 0),
              require = 2)
    private boolean sortAndAddRendererUpdateQueue(List instance, Object renderer) {
        return false;
    }

    @Inject(method = "updateRenderers",
            at = @At("HEAD"),
            require = 1,
            cancellable = true)
    private void performCullingUpdates(EntityLivingBase view, boolean p_72716_2_, CallbackInfoReturnable<Boolean> cir) {
        OcclusionHelpers.renderer.performCullingUpdates(view, p_72716_2_);
        cir.setReturnValue(true);
    }

    @Inject(method = "setWorldAndLoadRenderers",
            at = @At("HEAD"),
            require = 1)
    private void setWorkerWorld(WorldClient world, CallbackInfo ci) {
        OcclusionHelpers.worker.setWorld((RenderGlobal) (Object) this, world);
    }

    @Inject(method = "loadRenderers",
            at = @At("HEAD"),
            require = 1)
    private void resetLoadedRenderers(CallbackInfo ci) {
        OcclusionHelpers.renderer.resetLoadedRenderers();
    }

    @Inject(method = "loadRenderers",
            at = @At("TAIL"),
            require = 1)
    private void resetOcclusionWorker(CallbackInfo ci) {
        OcclusionHelpers.renderer.resetOcclusionWorker();
    }

    @Redirect(method = "loadRenderers",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/Arrays;sort([Ljava/lang/Object;Ljava/util/Comparator;)V",
                       ordinal = 0),
              require = 1)
    private void skipSort2(Object[] ts, Comparator<?> comparator) {

    }

    @Redirect(method = "loadRenderers",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/WorldRenderer;markDirty()V",
                       ordinal = 0),
              require = 1)
    private void markRendererInvisible(WorldRenderer instance) {
        OcclusionHelpers.renderer.markRendererInvisible(instance);
    }

    @Redirect(method = "loadRenderers",
              at = @At(value = "INVOKE",
                       target = "Ljava/nio/IntBuffer;get(I)I"),
              require = 1)
    private int padOcclusionQueryForShaders(IntBuffer instance, int i) {
        return Compat.optiFineHasShaders() ? i * 2 : i;
    }

    @Redirect(method = "loadRenderers",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/client/renderer/RenderGlobal;occlusionEnabled:Z",
                       opcode = Opcodes.GETFIELD),
              require = 1)
    private boolean noOcclusionInLoad(RenderGlobal instance) {
        return false;
    }

    @Redirect(method = "markRenderersForNewPosition",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/WorldRenderer;setPosition(III)V"),
              require = 1)
    private void setPositionAndMarkInvisible(WorldRenderer wr, int x, int y, int z) {
        OcclusionHelpers.renderer.setPositionAndMarkInvisible(wr, x, y, z);
    }

    @Inject(method = "markRenderersForNewPosition",
            at = @At("TAIL"),
            require = 1)
    private void runWorker(int p_72722_1_, int p_72722_2_, int p_72722_3_, CallbackInfo ci) {
        OcclusionHelpers.renderer.runWorkerFull();
    }

    /**
     * @author skyboy, embeddedt
     * @reason Update logic
     */
    @Overwrite
    public int sortAndRender(EntityLivingBase view, int pass, double tick) {
        return OcclusionHelpers.renderer.sortAndRender(view, pass, tick);
    }

    /**
     * @author embeddedt, skyboy
     * @reason occlusion culling
     */
    @Overwrite
    public int renderSortedRenderers(int start, int end, int pass, double tick) {
        return OcclusionHelpers.renderer.sortAndRender(start, end, pass, tick);
    }

    /**
     * @author makamys
     * @reason Integrate with the logic in {@link OcclusionWorker#run(boolean)}.
     */
    @Overwrite
    public void clipRenderersByFrustum(ICamera p_72729_1_, float p_72729_2_) {
        OcclusionHelpers.renderer.clipRenderersByFrustum(p_72729_1_, p_72729_2_);
    }

    @Redirect(method = "<init>",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/client/renderer/RenderGlobal;occlusionEnabled:Z",
                       opcode = Opcodes.GETFIELD,
                       ordinal = 0),
              require = 1)
    private boolean noBuiltinQueries(RenderGlobal instance) {
        return false;
    }

    @Redirect(method = "<init>",
              slice = @Slice(from = @At(value = "INVOKE",
                                        target = "Lnet/minecraft/client/Minecraft;getTextureManager()Lnet/minecraft/client/renderer/texture/TextureManager;")),
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/GLAllocation;generateDisplayLists(I)I",
                       ordinal = 0),
              require = 1)
    private int removeCreate(int p_74526_0_) {
        return -1;
    }

    @Redirect(method = "deleteAllDisplayLists",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/GLAllocation;deleteDisplayLists(I)V"),
              require = 1)
    private void removeDelete(int list) {
    }
}
