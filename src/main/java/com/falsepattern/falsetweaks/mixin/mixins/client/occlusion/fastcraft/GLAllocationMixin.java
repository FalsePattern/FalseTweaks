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

package com.falsepattern.falsetweaks.mixin.mixins.client.occlusion.fastcraft;

import lombok.val;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.GLAllocation;

import java.util.Map;

@Mixin(GLAllocation.class)
public abstract class GLAllocationMixin {
    @Shadow
    @Final
    private static Map<Integer, Integer> mapDisplayLists;

    @Inject(method = "deleteDisplayLists",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private static void nullSafeDealloc(int p_74523_0_, CallbackInfo ci) {
        if (!mapDisplayLists.containsKey(p_74523_0_)) {
            ci.cancel();
        }
    }

    @Inject(method = "generateDisplayLists",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private static void callerSensitiveAlloc(int p_74526_0_, CallbackInfoReturnable<Integer> cir) {
        val trace = Thread.currentThread().getStackTrace()[3];
        if (trace.getMethodName().equals("a") && trace.getClassName().equals("fastcraft.ak")) {
            //Line 336 in FastCraft 1.23
            //Line 53 in FastCraft 1.25
            //^ If anything breaks, check if the caller points are precisely these
            cir.setReturnValue(-1);
        }
    }
}

