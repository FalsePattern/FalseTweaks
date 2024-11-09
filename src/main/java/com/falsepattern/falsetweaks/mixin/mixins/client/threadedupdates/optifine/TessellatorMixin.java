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
package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates.optifine;

import com.falsepattern.falsetweaks.modules.threadedupdates.ITessellatorOptiFineCompat;
import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadedChunkUpdateHelper;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

@Mixin(Tessellator.class)
public abstract class TessellatorMixin implements ITessellatorOptiFineCompat {
    // This field has an odd name because of optifine compat (cAnNoT aLiAs NoN-pRiVaTe MeMbEr -- SpongePowered Mixins)
    @Shadow(aliases = {"rawBufferSize"}, remap = false)
    public int field_78388_E;
    @Dynamic
    @Shadow
    private ByteBuffer byteBuffer;
    @Dynamic
    @Shadow
    private IntBuffer intBuffer;
    @Dynamic
    @Shadow
    private FloatBuffer floatBuffer;
    @Dynamic
    @Shadow
    private ShortBuffer shortBuffer;

    @Dynamic
    @Redirect(method = "addVertex",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/GLAllocation;createDirectByteBuffer(I)Ljava/nio/ByteBuffer;"),
              require = 1)
    private ByteBuffer noReallocateNonMainTessellators(int size) {
        if ((Object) this != ThreadedChunkUpdateHelper.mainThreadTessellator()) {
            return byteBuffer;
        }
        return GLAllocation.createDirectByteBuffer(size);
    }

    @Override
    public void ft$resizeNativeBuffers() {
        byteBuffer = GLAllocation.createDirectByteBuffer(field_78388_E * 4);
        intBuffer = byteBuffer.asIntBuffer();
        floatBuffer = byteBuffer.asFloatBuffer();
        shortBuffer = byteBuffer.asShortBuffer();
    }
}
