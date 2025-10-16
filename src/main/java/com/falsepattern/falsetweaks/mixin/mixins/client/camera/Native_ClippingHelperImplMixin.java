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

package com.falsepattern.falsetweaks.mixin.mixins.client.camera;

import com.falsepattern.falsetweaks.modules.natives.Natives;
import com.falsepattern.falsetweaks.modules.natives.camera.Clipping;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;

@Mixin(value = ClippingHelperImpl.class,
       priority = 1100)
public abstract class Native_ClippingHelperImplMixin extends ClippingHelper {
    @Unique
    private float[] ft$nativeUploadFrustumArray;

    @Inject(method = "init",
            at = @At("RETURN"),
            require = 1)
    private void upload(CallbackInfo ci) {
        if (Natives.isLoaded()) {
            val frustum = this.frustum;
            final float[] frust;
            if (this.ft$nativeUploadFrustumArray == null) {
                this.ft$nativeUploadFrustumArray = frust = new float[24];
            } else {
                frust = this.ft$nativeUploadFrustumArray;
            }
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 6; j++) {
                    frust[i * 6 + j] = frustum[j][i];
                }
            }
            Clipping.setFrustum(frust);
        }
    }
}
