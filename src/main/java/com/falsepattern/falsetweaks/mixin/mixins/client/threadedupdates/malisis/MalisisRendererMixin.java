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

package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates.malisis;

import com.falsepattern.falsetweaks.api.threading.ThreadSafeBlockRenderer;
import net.malisis.core.renderer.IRenderWorldLast;
import net.malisis.core.renderer.MalisisRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;

import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Mixin(MalisisRenderer.class)
public abstract class MalisisRendererMixin extends TileEntitySpecialRenderer
        implements ISimpleBlockRenderingHandler, IItemRenderer, IRenderWorldLast, ThreadSafeBlockRenderer {
    @Unique
    private static final ReentrantLock ft$lock = new ReentrantLock();
    @Unique
    private static final AtomicInteger ft$lockCounter = new AtomicInteger(0);
    @Inject(method = "renderInventoryBlock",
            at = @At("HEAD"),
            remap = false,
            require = 1)
    private void beginRIB(Block block, int metadata, int modelId, RenderBlocks renderer, CallbackInfo ci) {
        ft$lock();
    }

    @Inject(method = "renderWorldBlock",
            at = @At("HEAD"),
            remap = false,
            require = 1)
    private void beginRWB(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer, CallbackInfoReturnable<Boolean> cir) {
        ft$lock();
    }

    @Inject(method = "renderItem",
            at = @At("HEAD"),
            remap = false,
            require = 1)
    private void beginRI(IItemRenderer.ItemRenderType type, ItemStack item, Object[] data, CallbackInfo ci) {
        ft$lock();
    }

    @Inject(method = "renderTileEntityAt",
            at = @At("HEAD"),
            require = 1)
    private void beginRTEA(TileEntity te, double x, double y, double z, float partialTick, CallbackInfo ci) {
        ft$lock();
    }

    @Inject(method = "renderWorldLastEvent",
            at = @At("HEAD"),
            remap = false,
            require = 1)
    private void beginRWLE(RenderWorldLastEvent event, IBlockAccess world, CallbackInfo ci) {
        ft$lock();
    }

    @Inject(method = "renderInventoryBlock",
            at = @At("RETURN"),
            remap = false,
            require = 1)
    private void endRIB(Block block, int metadata, int modelId, RenderBlocks renderer, CallbackInfo ci) {
        ft$unlock();
    }

    @Inject(method = "renderWorldBlock",
            at = @At("RETURN"),
            remap = false,
            require = 1)
    private void endRWB(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer, CallbackInfoReturnable<Boolean> cir) {
        ft$unlock();
    }

    @Inject(method = "renderItem",
            at = @At("RETURN"),
            remap = false,
            require = 1)
    private void endRI(IItemRenderer.ItemRenderType type, ItemStack item, Object[] data, CallbackInfo ci) {
        ft$unlock();
    }

    @Inject(method = "renderTileEntityAt",
            at = @At("RETURN"),
            require = 1)
    private void endRTEA(TileEntity te, double x, double y, double z, float partialTick, CallbackInfo ci) {
        ft$unlock();
    }

    @Inject(method = "renderWorldLastEvent",
            at = @At("RETURN"),
            remap = false,
            require = 1)
    private void endRWLE(RenderWorldLastEvent event, IBlockAccess world, CallbackInfo ci) {
        ft$unlock();
    }

    @Unique
    private static void ft$lock() {
        if (!ft$lock.isHeldByCurrentThread()) {
            while (!ft$lock.tryLock()) {
                Thread.yield();
            }
        }
        ft$lockCounter.incrementAndGet();
    }

    @Unique
    private static void ft$unlock() {
        if (ft$lockCounter.decrementAndGet() == 0) {
            ft$lock.unlock();
        }
    }

    @Override
    public ISimpleBlockRenderingHandler forCurrentThread() {
        return this;
    }
}
