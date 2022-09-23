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

package com.falsepattern.falsetweaks.mixin.mixins.client.optifine.leakfix;

import com.falsepattern.falsetweaks.leakfix.LeakFix;
import com.falsepattern.falsetweaks.mixin.stubpackage.WrDisplayListAllocator;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.RenderGlobal;

@SuppressWarnings({"InvalidInjectorMethodSignature", "UnresolvedMixinReference", "MixinAnnotationTarget"})
@Mixin(RenderGlobal.class)
public abstract class RenderGlobalOptiFineMixin {
    @Shadow(remap = false)
    public WrDisplayListAllocator displayListAllocator;

    @Redirect(method = "<init>",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/client/renderer/RenderGlobal;displayListAllocator:LWrDisplayListAllocator;",
                       opcode = Opcodes.PUTFIELD,
                       remap = false),
              require = 1)
    private void noAllocator(RenderGlobal rg, WrDisplayListAllocator param) {
        if (!LeakFix.ENABLED) {
            displayListAllocator = param;
        }
    }

    @Redirect(method = "loadRenderers",
              at = @At(value = "INVOKE",
                       target = "LWrDisplayListAllocator;resetAllocatedLists()V",
                       remap = false),
              require = 1)
    private void noReset(WrDisplayListAllocator allocator) {
        if (!LeakFix.ENABLED) {
            allocator.resetAllocatedLists();
        }
    }

    @Redirect(method = "deleteAllDisplayLists",
              at = @At(value = "INVOKE",
                       target = "LWrDisplayListAllocator;deleteDisplayLists()V",
                       remap = false),
              require = 1)
    private void noDelete(WrDisplayListAllocator allocator) {
        if (!LeakFix.ENABLED) {
            allocator.deleteDisplayLists();
        }
    }
}
