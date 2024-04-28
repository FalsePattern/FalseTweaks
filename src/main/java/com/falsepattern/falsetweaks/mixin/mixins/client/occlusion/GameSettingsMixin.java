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

package com.falsepattern.falsetweaks.mixin.mixins.client.occlusion;

import com.falsepattern.falsetweaks.config.OcclusionConfig;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.client.settings.GameSettings;

@Mixin(GameSettings.class)
public abstract class GameSettingsMixin {
    @Dynamic
    @ModifyConstant(method = "<init>(Lnet/minecraft/client/Minecraft;Ljava/io/File;)V",
                    slice = @Slice(from = @At(value = "CONSTANT",
                                              args = "stringValue=options.txt")),
                    constant = {@Constant(floatValue = 16.0F,
                                          ordinal = 0),
                                @Constant(floatValue = 32.0F, //optifine, fastcraft (patched)
                                          ordinal = 0)},
                    require = 1)
    private static float expandRenderDistance(float constant) {
        return OcclusionConfig.RENDER_DISTANCE;
    }
}
