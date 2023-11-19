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

package com.falsepattern.falsetweaks.mixin.mixins.client.triangulator;

import com.falsepattern.falsetweaks.modules.triangulator.interfaces.IRenderBlocksMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;

@Mixin(RenderBlocks.class)
public abstract class RenderBlocksCompatMixin implements IRenderBlocksMixin {
    @Inject(method = {"renderStandardBlockWithAmbientOcclusion", "renderStandardBlockWithAmbientOcclusionPartial"},
            at = @At("HEAD"),
            cancellable = true,
            require = 2)
    public void renderWithAOHook(Block block, int x, int y, int z, float r, float g, float b, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(renderWithAO(block, x, y, z, r, g, b));
    }
}
