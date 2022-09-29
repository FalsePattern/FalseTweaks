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

package com.falsepattern.falsetweaks.mixin.mixins.client.voxelizer;

import com.falsepattern.falsetweaks.config.VoxelizerConfig;
import com.falsepattern.falsetweaks.modules.voxelizer.VoxelRenderHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockRailBase;
import net.minecraft.client.renderer.RenderBlocks;

@Mixin(RenderBlocks.class)
public abstract class RenderBlocksMixin {
    @Inject(method = "renderBlockMinecartTrack",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private void renderRailCustom(BlockRailBase rail, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        if (!VoxelizerConfig.RAILS_3D) {
            return;
        }
        VoxelRenderHelper.renderRail((RenderBlocks)(Object)this, rail, x, y, z);
        cir.setReturnValue(true);
    }

}
