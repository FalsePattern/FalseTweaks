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

package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates;

import com.falsepattern.falsetweaks.config.ThreadingConfig;
import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadTessellator;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.Tessellator;

import java.nio.Buffer;
import java.nio.ByteBuffer;

@Mixin(Tessellator.class)
public abstract class TessellatorMixin {
    @Shadow
    private boolean isDrawing;
    @Unique
    private Throwable ft$lastStart;

    /**
     * @reason Allow using multiple tessellator instances concurrently by removing static field access in alternate
     * instances.
     */
    @WrapOperation(method = "reset",
                   at = @At(value = "INVOKE",
                            target = "Ljava/nio/ByteBuffer;clear()Ljava/nio/Buffer;"),
                   require = 1)
    private Buffer removeStaticBufferAccessOutsideSingleton(ByteBuffer instance, Operation<Buffer> original) {
        if (((Object) this) == ThreadTessellator.mainThreadTessellator()) {
            instance.clear();
        }
        return instance;
    }

    @Inject(method = "startDrawingQuads",
            at = @At("HEAD"),
            require = 1)
    private void startedAt(CallbackInfo ci) {
        if (!ThreadingConfig.EXTRA_DEBUG_INFO) {
            return;
        }

        if (isDrawing) {
            throw new IllegalStateException("Already tessellating!", ft$lastStart);
        }
        ft$lastStart = new Throwable();
    }
}
