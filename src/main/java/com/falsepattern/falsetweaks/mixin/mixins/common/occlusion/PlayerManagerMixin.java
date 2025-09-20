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

package com.falsepattern.falsetweaks.mixin.mixins.common.occlusion;

import com.falsepattern.falsetweaks.config.RenderDistanceConfig;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.server.management.PlayerManager;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Dynamic
    @ModifyConstant(method = "func_152622_a",
                    constant = {@Constant(intValue = 20,
                                          ordinal = 0),
                                @Constant(intValue = 32,
                                          ordinal = 0)},
                    require = 0,
                    expect = 0)
    private int expandViewDistance(int constant) {
        return RenderDistanceConfig.RENDER_DISTANCE;
    }
}
