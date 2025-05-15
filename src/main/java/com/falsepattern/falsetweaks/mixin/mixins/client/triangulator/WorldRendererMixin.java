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

package com.falsepattern.falsetweaks.mixin.mixins.client.triangulator;

import com.falsepattern.falsetweaks.Compat;
import com.falsepattern.falsetweaks.api.triangulator.ToggleableTessellator;
import com.falsepattern.falsetweaks.modules.triangulator.ToggleableTessellatorManager;
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
        ToggleableTessellatorManager.preRenderBlocks(pass);
    }

    @Inject(method = "postRenderBlocks",
            at = @At(value = "RETURN"),
            require = 1)
    private void noTriOnPass1Post(int pass, EntityLivingBase p_147891_2_, CallbackInfo ci) {
        ToggleableTessellatorManager.postRenderBlocks(pass);
    }
}
