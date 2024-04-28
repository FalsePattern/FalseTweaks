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

package com.falsepattern.falsetweaks.mixin.mixins.client.occlusion.optifine;

import com.falsepattern.falsetweaks.config.OcclusionConfig;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.settings.GameSettings;

@Mixin(GameSettings.class)
public abstract class GameSettingsOptifineMixin {
    @Dynamic
    @Shadow(remap = false)
    public int ofChunkLoading;

    @Shadow
    public boolean advancedOpengl;

    @Dynamic
    @Shadow(remap = false)
    public boolean ofOcclusionFancy;

    @Dynamic
    @ModifyConstant(method = "loadOfOptions",
                    constant = @Constant(intValue = 32),
                    require = 1)
    private int changeRenderDistanceOnSave(int oldRenderDistance) {
        return OcclusionConfig.RENDER_DISTANCE;
    }

    @Dynamic
    @Inject(method = "updateChunkLoading",
            at = @At(value = "HEAD"),
            remap = false,
            require = 1)
    private void noChunkLoading(CallbackInfo ci) {
        ofChunkLoading = 0;
    }

    @Redirect(method = "loadOptions",
              at = @At(value = "FIELD",
                       opcode = Opcodes.PUTFIELD,
                       target = "Lnet/minecraft/client/settings/GameSettings;advancedOpengl:Z"),
              require = 1)
    private void noAdvancedOpengl(GameSettings instance, boolean value) {
        advancedOpengl = false;
    }

    @Dynamic
    @Redirect(method = "loadOfOptions",
              at = @At(value = "FIELD",
                       opcode = Opcodes.PUTFIELD,
                       target = "Lnet/minecraft/client/settings/GameSettings;ofOcclusionFancy:Z"),
              require = 1)
    private void noOcclusionFancy(GameSettings instance, boolean value) {
        ofOcclusionFancy = false;
    }
}
