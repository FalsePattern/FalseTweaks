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

package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates;

import com.falsepattern.falsetweaks.modules.threadedupdates.ICapturableTessellator;
import com.falsepattern.falsetweaks.modules.threadedupdates.IRendererUpdateResultHolder;
import com.falsepattern.falsetweaks.modules.threadedupdates.NeodymiumCompat;
import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadedChunkUpdateHelper;
import com.llamalad7.mixinextras.sugar.Local;
import lombok.val;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.shader.TesselatorVertexState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.ForgeHooksClient;

import java.util.List;

import static com.falsepattern.falsetweaks.modules.threadedupdates.ThreadedChunkUpdateHelper.AGGRESSIVE_NEODYMIUM_THREADING;

@Mixin(value = WorldRenderer.class)
public abstract class WorldRendererMixin implements IRendererUpdateResultHolder {

    @Shadow
    public List<TileEntity> tileEntityRenderers;
    private ThreadedChunkUpdateHelper.UpdateTask arch$updateTask;
    @Unique
    private int ft$pass;
    @Dynamic
    @Shadow(remap = false)
    private boolean ft$needsSort;

    @Shadow
    protected abstract void postRenderBlocks(int p_147891_1_, EntityLivingBase p_147891_2_);

    @Shadow
    protected abstract void preRenderBlocks(int p_147890_1_);

    @Inject(method = "updateRenderer",
            at = @At("HEAD"),
            require = 1)
    private void setLastWorldRendererSingleton(CallbackInfo ci) {
        ThreadedChunkUpdateHelper.lastWorldRenderer = ((WorldRenderer) (Object) this);
    }

    @Inject(method = "updateRenderer",
            at = @At("RETURN"),
            require = 1)
    private void clearLastWorldRenderer(EntityLivingBase p_147892_1_, CallbackInfo ci) {
        ThreadedChunkUpdateHelper.lastWorldRenderer = null;
    }

    @ModifyConstant(method = "updateRenderer",
                    constant = @Constant(intValue = 16),
                    require = 3)
    private int killIteration(int constant) {
        return AGGRESSIVE_NEODYMIUM_THREADING ? 0 : constant;
    }

    @SuppressWarnings("unused")
    @Dynamic("Called by com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates.WorldRenderer_NonOptiFineMixin and com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates.optifine.WorldRendererMixin")
    @Unique
    private int ft$insertNextPass(int pass) {
        if (!AGGRESSIVE_NEODYMIUM_THREADING) {
            return pass;
        }
        ft$pass = pass;
        val task = ((IRendererUpdateResultHolder) ThreadedChunkUpdateHelper.lastWorldRenderer).ft$getRendererUpdateTask();
        if (task != null && !task.cancelled && pass >= 0) {
            return task.result[pass].nextPass ? 1 : 0;
        }
        return 0;
    }

    @ModifyConstant(method = "updateRenderer",
                    constant = @Constant(intValue = 0,
                                         ordinal = 2),
                    slice = @Slice(from = @At(value = "FIELD",
                                              target = "Lnet/minecraft/client/renderer/WorldRenderer;vertexState:Lnet/minecraft/client/shader/TesselatorVertexState;",
                                              shift = At.Shift.AFTER,
                                              ordinal = 0)),
                    require = 1)
    private int insertRenderedSomething(int constant) {
        if (!AGGRESSIVE_NEODYMIUM_THREADING) {
            return constant;
        }
        val pass = ft$pass;
        val task = ((IRendererUpdateResultHolder) this).ft$getRendererUpdateTask();
        if (task != null && !task.cancelled && pass >= 0) {
            return task.result[pass].renderedSomething ? 1 : 0;
        }
        return 0;
    }

    @ModifyConstant(method = "updateRenderer",
                    constant = @Constant(intValue = 0,
                                         ordinal = 3),
                    slice = @Slice(from = @At(value = "FIELD",
                                              target = "Lnet/minecraft/client/renderer/WorldRenderer;vertexState:Lnet/minecraft/client/shader/TesselatorVertexState;",
                                              shift = At.Shift.AFTER,
                                              ordinal = 0)),
                    require = 1)
    private int insertStartedTessellator(int constant) {
        if (!AGGRESSIVE_NEODYMIUM_THREADING) {
            return constant;
        }
        val pass = ft$pass;
        val task = ft$getRendererUpdateTask();
        if (task != null && !task.cancelled && pass >= 0) {
            boolean startedTessellator = task.result[pass].startedTessellator;
            if (startedTessellator) {
                NeodymiumCompat.beginMainThreadRenderPass((WorldRenderer) (Object) this, task, pass);
            }
            return startedTessellator ? 1 : 0;
        }
        return 0;
    }

    @Redirect(method = "updateRenderer",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/WorldRenderer;postRenderBlocks(ILnet/minecraft/entity/EntityLivingBase;)V"),
              require = 1)
    private void loadTessellationResult(WorldRenderer instance, int pass, EntityLivingBase entity) {
        if (AGGRESSIVE_NEODYMIUM_THREADING) {
            net.minecraftforge.client.ForgeHooksClient.onPostRenderWorld((WorldRenderer) (Object) this, pass);
        } else {
            if (!ft$getRendererUpdateTask().cancelled) {
                ((ICapturableTessellator) Tessellator.instance).arch$addTessellatorVertexState(ft$getRendererUpdateTask().result[pass].renderedQuads());
            }
            postRenderBlocks(pass, entity);
        }
    }

    @Inject(method = "updateRenderer",
            at = @At(value = "INVOKE",
                     target = "Ljava/util/HashSet;addAll(Ljava/util/Collection;)Z",
                     ordinal = 0),
            slice = @Slice(from = @At(value = "INVOKE",
                                      target = "Lnet/minecraft/client/renderer/WorldRenderer;postRenderBlocks(ILnet/minecraft/entity/EntityLivingBase;)V")),
            require = 1)
    private void addThreadRenderers(EntityLivingBase p_147892_1_, CallbackInfo ci) {
        if (!AGGRESSIVE_NEODYMIUM_THREADING) {
            return;
        }
        val task = ft$getRendererUpdateTask();
        if (task != null && !task.cancelled) {
            tileEntityRenderers.addAll(task.TESRs);
            task.TESRs.clear();
        }
    }

    @Override
    public ThreadedChunkUpdateHelper.UpdateTask ft$getRendererUpdateTask() {
        if (arch$updateTask == null) {
            arch$updateTask = new ThreadedChunkUpdateHelper.UpdateTask();
        }
        return arch$updateTask;
    }

    @Inject(method = "markDirty",
            at = @At("RETURN"),
            require = 1)
    private void notifyDirty(CallbackInfo ci) {
        ThreadedChunkUpdateHelper.instance.onWorldRendererDirty((WorldRenderer) (Object) this);
    }

    @Inject(method = "markDirty",
            at = @At("HEAD"),
            require = 1)
    private void unMarkSort(CallbackInfo ci) {
        ft$needsSort = false;
    }

    @Redirect(method = "updateRendererSort",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/client/renderer/WorldRenderer;vertexState:Lnet/minecraft/client/shader/TesselatorVertexState;",
                       ordinal = 0),
              require = 1)
    private TesselatorVertexState hijackSort(WorldRenderer wr, @Local(argsOnly = true) EntityLivingBase player) {
        if (!AGGRESSIVE_NEODYMIUM_THREADING) {
            return wr.vertexState;
        }

        if (wr.skipRenderPass[1]) {
            return null;
        }

        val task = ((IRendererUpdateResultHolder) wr).ft$getRendererUpdateTask();
        NeodymiumCompat.beginMainThreadRenderPass(wr, task, 1);
        ForgeHooksClient.onPostRenderWorld(wr, 1);

        return null;
    }
}
