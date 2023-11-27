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

import com.falsepattern.falsetweaks.modules.triangulator.interfaces.ITessellatorMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.shader.TesselatorVertexState;

@Mixin(Tessellator.class)
public abstract class TessellatorBSPSortingMixin implements ITessellatorMixin {
    /**
     * @author FalsePattern
     * @reason BSP sorting, way less allocs than inject/cancel
     */
    @Overwrite
    public TesselatorVertexState getVertexState(float p_147564_1_, float p_147564_2_, float p_147564_3_) {
        return getVertexStateBSP(p_147564_1_, p_147564_2_, p_147564_3_);
    }

    @Inject(method = "setVertexState",
            at = @At(value = "HEAD"),
            require = 1)
    private void bspSort(TesselatorVertexState p_147565_1_, CallbackInfo ci) {
        setVertexStateBSP(p_147565_1_);
    }
}
