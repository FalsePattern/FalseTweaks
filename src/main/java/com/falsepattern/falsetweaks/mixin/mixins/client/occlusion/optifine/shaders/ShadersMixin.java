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

import com.falsepattern.falsetweaks.modules.occlusion.shader.ShadowPassOcclusionHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shadersmod.client.Shaders;

import java.nio.FloatBuffer;

@Mixin(value = Shaders.class,
       remap = false)
public abstract class ShadersMixin {
    @Shadow @Final static FloatBuffer shadowModelView;

    @Inject(method = "setCameraShadow",
            at = @At("RETURN"),
            require = 1)
    private static void onSetCameraShadow(CallbackInfo ci) {
        shadowModelView.position(0);
        ShadowPassOcclusionHelper.shadowModelViewMatrix.set(shadowModelView);
    }
}
