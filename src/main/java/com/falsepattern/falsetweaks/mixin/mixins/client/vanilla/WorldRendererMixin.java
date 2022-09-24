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

package com.falsepattern.falsetweaks.mixin.mixins.client.vanilla;

import com.falsepattern.falsetweaks.TriCompat;
import com.falsepattern.falsetweaks.api.ToggleableTessellator;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.EntityLivingBase;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Inject(method = "preRenderBlocks",
            at = @At("HEAD"),
            require = 1)
    private void noTriOnPass1Pre(int pass, CallbackInfo ci) {
        val tess = (ToggleableTessellator) TriCompat.tessellator();
        tess.pass(pass);
        if (pass != 0) {
            tess.disableTriangulatorLocal();
        }
    }

    @Inject(method = "postRenderBlocks",
            at = @At(value = "RETURN"),
            require = 1)
    private void noTriOnPass1Post(int pass, EntityLivingBase p_147891_2_, CallbackInfo ci) {
        if (pass != 0) {
            ((ToggleableTessellator)TriCompat.tessellator()).enableTriangulatorLocal();
        }
    }
}
