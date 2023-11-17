/*
 * FalseTweaks
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

package com.falsepattern.falsetweaks.mixin.mixins.client.occlusion;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.client.settings.GameSettings;

@Mixin(GameSettings.class)
public abstract class GameSettingsMixin {
    @ModifyConstant(method = "<init>(Lnet/minecraft/client/Minecraft;Ljava/io/File;)V",
                    slice = @Slice(from = @At(value = "CONSTANT",
                                              args = "stringValue=options.txt")),
                    constant = @Constant(floatValue = 16.0F,
                                         ordinal = 0),
                    require = 1)
    private static float expandRenderDistance(float constant) {
        return 64.0F;
    }
}
