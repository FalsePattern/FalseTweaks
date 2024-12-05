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

package com.falsepattern.falsetweaks.mixin.plugin.init;

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

    ASMDataTableMixin(Side.COMMON, condition(() -> ModuleConfig.STARTUP_OPTIMIZATIONS), "startup.ASMDataTableMixin"),

    DirectoryDiscovererMixin(Side.COMMON, condition(() -> ModuleConfig.STARTUP_OPTIMIZATIONS), "startup.DirectoryDiscovererMixin"),
    JarDiscovererMixin(Side.COMMON, condition(() -> ModuleConfig.STARTUP_OPTIMIZATIONS), "startup.JarDiscovererMixin"),
    ModContainerFactoryMixin(Side.COMMON, condition(() -> ModuleConfig.STARTUP_OPTIMIZATIONS), "startup.ModContainerFactoryMixin"),
    ModDiscovererMixin(Side.COMMON, condition(() -> ModuleConfig.STARTUP_OPTIMIZATIONS), "startup.ModDiscovererMixin"),

    //endregion Startup Optimizations Module
    // @formatter:on
    ;

    @Getter
    private final Side side;
    @Getter
    private final Predicate<List<ITargetedMod>> filter;
    @Getter
    private final String mixin;

    private static class Detections {
        private static final int JAVA_VERSION = getVersion();
        private static final Predicate<List<ITargetedMod>> JAVA8 = condition(() -> JAVA_VERSION == 8);

        private static int getVersion() {
            String version = System.getProperty("java.version");
            if (version.startsWith("1.")) {
                version = version.substring(2, 3);
            } else {
                int dot = version.indexOf(".");
                if (dot != -1) {
                    version = version.substring(0, dot);
                }
            }
            try {
                return Integer.parseInt(version);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return -1;
            }
        }
    }
}

