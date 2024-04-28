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
package com.falsepattern.falsetweaks.mixin.mixins.client.occlusion.optifine.shaders;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.Frustrum;

@Mixin(Frustrum.class)
public abstract class FrustrumMixin {
    @Dynamic
    @Redirect(method = "<init>(Lnet/minecraft/client/renderer/culling/ClippingHelper;)V",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/culling/ClippingHelperImpl;getInstance()Lnet/minecraft/client/renderer/culling/ClippingHelper;"),
              require = 1)
    private ClippingHelper noClippingHelperForShadow() {
        return null;
    }
}
