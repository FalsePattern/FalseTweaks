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

package com.falsepattern.falsetweaks.mixin.mixins.client.optispam;

import com.falsepattern.falsetweaks.config.OptiSpamConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import shadersmod.common.SMCLog;
import shadersmod.uniform.ShaderExpressionResolver;

@Mixin(value = ShaderExpressionResolver.class,
       remap = false)
public abstract class ShaderExpressionResolverMixin {
    @Redirect(method = "registerExpression",
              at = @At(value = "INVOKE",
                       target = "Lshadersmod/common/SMCLog;warning(Ljava/lang/String;)V"))
    private void suppressWarn(String message) {
        if (!OptiSpamConfig.CUSTOM_UNIFORMS) {
            SMCLog.warning(message);
        }
    }
}
