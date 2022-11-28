/*
 * FalseTweaks
 *
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice, this permission notice and the word "SNEED"
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.falsetweaks.mixin.mixins.client.voxelizer.railcraft;

import com.falsepattern.falsetweaks.config.VoxelizerConfig;
import com.falsepattern.falsetweaks.modules.voxelizer.VoxelRenderHelper;
import mods.railcraft.api.tracks.ITrackInstance;
import mods.railcraft.api.tracks.ITrackSwitch;
import mods.railcraft.client.render.RenderTrack;
import mods.railcraft.common.blocks.tracks.TileTrack;
import mods.railcraft.common.blocks.tracks.TrackGated;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

@Mixin(value = RenderTrack.class,
       remap = false)
public abstract class RenderTrackMixin {

    @Shadow
    private static void renderGatedTrack(RenderBlocks render, TrackGated track, int i, int j, int k, int meta) {
    }

    @Inject(method = "renderWorldBlock",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    public void renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderblocks, CallbackInfoReturnable<Boolean> cir) {
        if (!VoxelizerConfig.RAILS_3D) {
            return;
        }
        cir.setReturnValue(true);
        int meta = ((BlockRailBase) block).getBasicRailMetadata(world, null, x, y, z);
        TileEntity tile = world.getTileEntity(x, y, z);
        ITrackInstance track = null;
        IIcon icon;
        if (tile instanceof TileTrack) {
            track = ((TileTrack) tile).getTrackInstance();
            icon = renderblocks.getIconSafe(track.getIcon());
        } else {
            icon = Blocks.rail.getIcon(0, 0);
        }

        if (renderblocks.hasOverrideBlockTexture()) {
            icon = renderblocks.overrideBlockTexture;
        }
        boolean mirror = false;
        if (track != null) {
            if (track instanceof ITrackSwitch) {
                ITrackSwitch switchTrack = (ITrackSwitch) track;
                mirror = switchTrack.isMirrored();
            } else if (track instanceof TrackGated) {
                renderGatedTrack(renderblocks, (TrackGated) track, x, y, z, meta);
            }
        }
        VoxelRenderHelper.renderRail(world, (BlockRailBase) block, x, y, z, meta, icon, mirror);
    }
}
