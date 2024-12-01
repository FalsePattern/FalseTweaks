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

package com.falsepattern.falsetweaks.mixin.mixins.client.optispam;

import com.falsepattern.falsetweaks.config.OptiSpamConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import stubpackage.Config;

@Mixin(targets = "ConnectedParser",
       remap = false)
public abstract class ConnectedParserMixin {
    @Redirect(method = "warn",
              at = @At(value = "INVOKE",
                       target = "LConfig;warn(Ljava/lang/String;)V"))
    private void suppressWarn(String s) {
        if (!OptiSpamConfig.BLOCK_NOT_FOUND) {
            Config.warn(s);
        }
    }
}
