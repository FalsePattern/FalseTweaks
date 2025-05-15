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

package com.falsepattern.falsetweaks.mixin.plugin.standard;

import com.falsepattern.lib.mixin.ITargetedMod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import net.minecraft.launchwrapper.Launch;

import java.util.List;
import java.util.function.Predicate;

import static com.falsepattern.falsetweaks.mixin.plugin.standard.Extras.OPTIFINE_DYNAMIC_LIGHTS_VERSIONS;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.Extras.OPTIFINE_SHADERSMOD_VERSIONS;
import static com.falsepattern.lib.mixin.IMixin.PredicateHelpers.avoid;
import static com.falsepattern.lib.mixin.IMixin.PredicateHelpers.condition;
import static com.falsepattern.lib.mixin.IMixin.PredicateHelpers.require;
import static com.falsepattern.lib.mixin.ITargetedMod.PredicateHelpers.contains;

@RequiredArgsConstructor
public enum TargetedMod implements ITargetedMod {
    FOAMFIX("FoamFix", false, contains("foamfix")),
    OPTIFINE_WITHOUT_SHADERS("OptiFine without shaders", false, contains("optifine").and(OPTIFINE_SHADERSMOD_VERSIONS.or(OPTIFINE_DYNAMIC_LIGHTS_VERSIONS).negate())),
    OPTIFINE_WITH_SHADERS("OptiFine with shaders", false, contains("optifine").and(OPTIFINE_SHADERSMOD_VERSIONS)),
    OPTIFINE_WITH_DYNAMIC_LIGHTS("OptiFine with dynamic lights", false, contains("optifine").and(OPTIFINE_DYNAMIC_LIGHTS_VERSIONS)),
    FASTCRAFT("FastCraft", false, contains("fastcraft")),
    REDSTONEPASTE("RedstonePaste", false, contains("redstonepaste")),
    APPARATUS("Apparatus", false, contains("apparatus")),
    RAILCRAFT("Railcraft", true, contains("railcraft")),
    DRAGONAPI("DragonAPI", false, contains("dragonapi")),
    NEODYMIUM("Neodymium", false, contains("neodymium-")),
    NUCLEAR_CONTROL("Nuclear Control 2", false, contains("IC2NuclearControl-")),
    OPEN_COMPUTERS("OpenComputers", false, contains("OpenComputers-")),
    COMPUTRONICS("Computronics", false, contains("Computronics-")),
    EXTRA_CELLS("Extra Cells 2", false, contains("ExtraCells-")),
    AUTOMAGY("Automagy", false, contains("Automagy-")),
    TECHGUNS("Techguns", false, contains("techguns")),
    MALISIS("Malisis Mods", false, contains("malisis")),
    MALISIS_NH("Malisis Mods NH", false, contains("malisis").and(contains("gtnh"))),
    NOTFINE("NotFine", false, contains("notfine-")),
    SECURITYCRAFT("SecurityCraft", false, contains("securitycraft")),
    STORAGE_DRAWERS("Storage Drawers", false, contains("StorageDrawers-1.7.10-")),
    THERMAL_EXPANSION("Thermal Expansion", false, contains("ThermalExpansion-")),
    ;

    public static final Predicate<List<ITargetedMod>> REQUIRE_OPTIFINE_WITHOUT_SHADERS = require(OPTIFINE_WITHOUT_SHADERS).or(require(OPTIFINE_WITH_DYNAMIC_LIGHTS));
    public static final Predicate<List<ITargetedMod>> REQUIRE_OPTIFINE_WITH_SHADERS = require(OPTIFINE_WITH_SHADERS);
    public static final Predicate<List<ITargetedMod>> AVOID_OPTIFINE_WITH_SHADERS = avoid(OPTIFINE_WITH_SHADERS);
    public static final Predicate<List<ITargetedMod>> AVOID_OPTIFINE_WITH_DYNAMIC_LIGHTS = avoid(OPTIFINE_WITH_SHADERS).and(avoid(OPTIFINE_WITH_DYNAMIC_LIGHTS));
    public static final Predicate<List<ITargetedMod>> REQUIRE_OPTIFINE_WITH_DYNAMIC_LIGHTS = require(OPTIFINE_WITH_SHADERS).or(require(OPTIFINE_WITH_DYNAMIC_LIGHTS));
    public static final Predicate<List<ITargetedMod>> REQUIRE_ANY_OPTIFINE = require(OPTIFINE_WITH_SHADERS).or(require(OPTIFINE_WITHOUT_SHADERS)).or(require(OPTIFINE_WITH_DYNAMIC_LIGHTS));
    public static final Predicate<List<ITargetedMod>> AVOID_ANY_OPTIFINE = avoid(OPTIFINE_WITH_SHADERS).and(avoid(OPTIFINE_WITHOUT_SHADERS)).and(avoid(OPTIFINE_WITH_DYNAMIC_LIGHTS));

    public static final Predicate<List<ITargetedMod>> DEV_ONLY = condition(() -> (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment"));
    public static final Predicate<List<ITargetedMod>> OBF_ONLY = condition(() -> !(Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment"));


    @Getter
    private final String modName;
    @Getter
    private final boolean loadInDevelopment;
    @Getter
    private final Predicate<String> condition;
}
