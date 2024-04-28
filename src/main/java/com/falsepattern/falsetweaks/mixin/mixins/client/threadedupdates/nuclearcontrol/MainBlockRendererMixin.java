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
package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates.nuclearcontrol;

import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadedChunkUpdateHelper;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shedar.mods.ic2.nuclearcontrol.renderers.MainBlockRenderer;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

@Mixin(value = MainBlockRenderer.class,
       remap = false)
public abstract class MainBlockRendererMixin {
    @Unique
    private static final int THERMAL_MONITOR_BLOCK_META = 0;
    @Unique
    private static final int INDUSTRIAL_ALARM_BLOCK_META = 1;
    @Unique
    private static final int HOWLER_ALARM_BLOCK_META = 2;
    @Unique
    private static final int REMOTE_THERMAL_MONITOR_BLOCK_META = 3;
    @Unique
    private static final int INFO_PANEL_BLOCK_META = 4;
    @Unique
    private static final int INFO_PANEL_EXTENDER_BLOCK_META = 5;
    @Unique
    private static final int ENERGY_COUNTER_BLOCK_META = 6;
    @Unique
    private static final int AVERAGE_COUNTER_BLOCK_META = 7;
    @Unique
    private static final int RANGE_TRIGGER_BLOCK_META = 8;
    @Unique
    private static final int ADVANCED_INFO_PANEL_BLOCK_META = 9;
    @Unique
    private static final int ADVANCED_INFO_PANEL_EXTENDER_BLOCK_META = 10;

    @Inject(method = "renderWorldBlock",
            at = @At("HEAD"),
            cancellable = true,
            require = 1)
    private void skipThreadedRendering(IBlockAccess world, int posX, int posY, int posZ, Block block, int model, RenderBlocks renderer, CallbackInfoReturnable<Boolean> cir) {
        val blockMeta = world.getBlockMetadata(posX, posY, posZ);
        switch (blockMeta) {
            case THERMAL_MONITOR_BLOCK_META:
            case INDUSTRIAL_ALARM_BLOCK_META:
            case HOWLER_ALARM_BLOCK_META:
            case REMOTE_THERMAL_MONITOR_BLOCK_META:
            case ENERGY_COUNTER_BLOCK_META:
            case AVERAGE_COUNTER_BLOCK_META:
            case RANGE_TRIGGER_BLOCK_META:
                return;
            default:
        }

        if (!ThreadedChunkUpdateHelper.isMainThread()) {
            cir.setReturnValue(false);
        }
    }

    @Redirect(method = "renderWorldBlock",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/RenderBlocks;renderStandardBlock(Lnet/minecraft/block/Block;III)Z",
                       remap = true),
              require = 3)
    private boolean renderInfoPanelBlocksWithoutAO(RenderBlocks renderBlocks, Block block, int posX, int posY, int posZ) {
        val world = renderBlocks.blockAccess;
        val blockMeta = world.getBlockMetadata(posX, posY, posZ);
        switch (blockMeta) {
            case INFO_PANEL_BLOCK_META:
            case INFO_PANEL_EXTENDER_BLOCK_META:
            case ADVANCED_INFO_PANEL_BLOCK_META:
            case ADVANCED_INFO_PANEL_EXTENDER_BLOCK_META:
                val color = block.colorMultiplier(world, posX, posY, posZ);
                val red = (color >> 16 & 255) / 255F;
                val green = (color >> 8 & 255) / 255F;
                val blue = (color & 255) / 255F;
                return renderBlocks.renderStandardBlockWithColorMultiplier(block, posX, posY, posZ, red, green, blue);
            default:
                return renderBlocks.renderStandardBlock(block, posX, posY, posZ);
        }
    }
}
