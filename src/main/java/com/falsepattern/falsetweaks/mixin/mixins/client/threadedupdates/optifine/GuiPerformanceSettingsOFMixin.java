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

import com.falsepattern.falsetweaks.modules.occlusion.OcclusionCompat;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;

@Pseudo
@Mixin(targets = "GuiPerformanceSettingsOF")
public abstract class GuiPerformanceSettingsOFMixin extends GuiScreen {
    @Dynamic
    @Shadow(remap = false)
    private static GameSettings.Options[] enumOptions;

    @Dynamic
    @Inject(method = "<clinit>",
            at = @At("RETURN"))
    private static void postCLInit(CallbackInfo ci) {
        enumOptions = OcclusionCompat.OptiFineCompat.filterOptions(enumOptions, (name) -> {
            switch (name) {
                case "FAST_RENDER":
                case "CHUNK_UPDATES":
                case "CHUNK_UPDATES_DYNAMIC":
                    return true;
            }
            return false;
        });
    }
}
