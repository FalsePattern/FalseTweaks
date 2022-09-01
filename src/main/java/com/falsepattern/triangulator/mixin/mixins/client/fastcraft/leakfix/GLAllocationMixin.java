/*
 * Triangulator
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

package com.falsepattern.triangulator.mixin.mixins.client.fastcraft.leakfix;

import com.falsepattern.triangulator.leakfix.LeakFix;
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
        if (LeakFix.ENABLED && !mapDisplayLists.containsKey(p_74523_0_)) {
            ci.cancel();
        }
    }

    @Inject(method = "generateDisplayLists",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private static void callerSensitiveAlloc(int p_74526_0_, CallbackInfoReturnable<Integer> cir) {
        if (LeakFix.ENABLED) {
            val trace = Thread.currentThread().getStackTrace()[3];
            if (trace.getMethodName().equals("a") && trace.getClassName().equals("fastcraft.ak")) {
                //Line 336 in FastCraft 1.23
                //Line 53 in FastCraft 1.25
                //^ If anything breaks, check if the caller points are precisely these
                cir.setReturnValue(-1);
            }
        }
    }
}
