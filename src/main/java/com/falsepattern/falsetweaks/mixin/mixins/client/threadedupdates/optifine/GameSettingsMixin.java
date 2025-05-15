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

package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates.optifine;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.settings.GameSettings;

@Mixin(GameSettings.class)
public abstract class GameSettingsMixin {
    @Dynamic
    @Shadow(remap = false)
    public boolean ofFastRender;

    @Dynamic
    @Shadow(remap = false)
    public int ofChunkUpdates;

    @Dynamic
    @Shadow(remap = false)
    public boolean ofChunkUpdatesDynamic;

    @Dynamic
    @Redirect(method = "loadOfOptions",
              at = @At(value = "FIELD",
                       opcode = Opcodes.PUTFIELD,
                       target = "Lnet/minecraft/client/settings/GameSettings;ofFastRender:Z",
                       remap = false),
              remap = false,
              require = 1)
    private void noFastRender(GameSettings instance, boolean value) {
        ofFastRender = false;
    }

    @Dynamic
    @Redirect(method = "loadOfOptions",
              at = @At(value = "FIELD",
                       opcode = Opcodes.PUTFIELD,
                       target = "Lnet/minecraft/client/settings/GameSettings;ofChunkUpdates:I",
                       remap = false),
              remap = false,
              require = 2)
    private void noChunkUpdates(GameSettings instance, int value) {
        ofChunkUpdates = 1;
    }

    @Dynamic
    @Redirect(method = "loadOfOptions",
              at = @At(value = "FIELD",
                       opcode = Opcodes.PUTFIELD,
                       target = "Lnet/minecraft/client/settings/GameSettings;ofChunkUpdatesDynamic:Z",
                       remap = false),
              remap = false,
              require = 1)
    private void noChunkUpdates(GameSettings instance, boolean value) {
        ofChunkUpdatesDynamic = false;
    }
}
