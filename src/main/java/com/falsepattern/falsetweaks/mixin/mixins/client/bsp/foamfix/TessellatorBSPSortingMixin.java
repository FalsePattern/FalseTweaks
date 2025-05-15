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

//keep in sync with TessellatorBSPSortingMixin
package com.falsepattern.falsetweaks.mixin.mixins.client.bsp.foamfix;

import com.falsepattern.falsetweaks.modules.bsp.IBSPTessellator;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.shader.TesselatorVertexState;

@Mixin(Tessellator.class)
public abstract class TessellatorBSPSortingMixin implements IBSPTessellator {
    @Dynamic
    @Inject(method = {"getVertexState", "getVertexState_foamfix_old"},
            at = @At(value = "HEAD"),
            cancellable = true)
    private void bspSort(float p_147564_1_, float p_147564_2_, float p_147564_3_, CallbackInfoReturnable<TesselatorVertexState> cir) {
        cir.setReturnValue(ft$getVertexStateBSP(p_147564_1_, p_147564_2_, p_147564_3_));
    }

    @Dynamic
    @Inject(method = {"setVertexState", "setVertexState_foamfix_old"},
            at = @At(value = "HEAD"))
    private void bspSort(TesselatorVertexState p_147565_1_, CallbackInfo ci) {
        ft$setVertexStateBSP(p_147565_1_);
    }
}
