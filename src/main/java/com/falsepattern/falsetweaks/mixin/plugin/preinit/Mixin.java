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

package com.falsepattern.falsetweaks.mixin.plugin.preinit;

import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.lib.mixin.IMixin;
import com.falsepattern.lib.mixin.ITargetedMod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Predicate;

import static com.falsepattern.lib.mixin.IMixin.PredicateHelpers.condition;

@RequiredArgsConstructor
public enum Mixin implements IMixin {
    // @formatter:off
    //region Startup Optimizations Module
    //region Minecraft->client
    DirectoryDiscovererMixin(Side.COMMON, condition(() -> ModuleConfig.STARTUP_OPTIMIZATIONS), "startup.DirectoryDiscovererMixin"),
    JarDiscovererMixin(Side.COMMON, condition(() -> ModuleConfig.STARTUP_OPTIMIZATIONS), "startup.JarDiscovererMixin"),
    ModContainerFactoryMixin(Side.COMMON, condition(() -> ModuleConfig.STARTUP_OPTIMIZATIONS), "startup.ModContainerFactoryMixin"),
    ModDiscovererMixin(Side.COMMON, condition(() -> ModuleConfig.STARTUP_OPTIMIZATIONS), "startup.ModDiscovererMixin"),
    //endregion Minecraft->client
    //endregion Startup Optimizations Module
    // @formatter:on
    ;

    @Getter
    private final Side side;
    @Getter
    private final Predicate<List<ITargetedMod>> filter;
    @Getter
    private final String mixin;
}

