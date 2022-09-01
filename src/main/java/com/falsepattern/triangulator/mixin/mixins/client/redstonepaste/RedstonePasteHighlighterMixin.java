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

package com.falsepattern.triangulator.mixin.mixins.client.redstonepaste;

import com.falsepattern.triangulator.api.ToggleableTessellator;
import fyber.redstonepastemod.client.RedstonePasteHighlighter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.Tessellator;

@Mixin(value = RedstonePasteHighlighter.class,
       remap = false)
public abstract class RedstonePasteHighlighterMixin {
    @Inject(method = "drawLineLoop",
            at = @At("HEAD"),
            require = 1)
    private void turnOffTriangulator(CallbackInfo ci) {
        ((ToggleableTessellator) Tessellator.instance).disableTriangulator();
    }

    @Inject(method = "drawLineLoop",
            at = @At("RETURN"),
            require = 1)
    private void turnOnTriangulator(CallbackInfo ci) {
        ((ToggleableTessellator) Tessellator.instance).enableTriangulator();
    }
}
