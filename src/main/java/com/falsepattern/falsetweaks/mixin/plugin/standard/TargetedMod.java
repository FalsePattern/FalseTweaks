/*
 * This file is part of FalseTweaks.
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

package com.falsepattern.falsetweaks.mixin.plugin.standard;

import com.falsepattern.lib.mixin.ITargetedMod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

import static com.falsepattern.falsetweaks.mixin.plugin.standard.Extras.OPTIFINE_SHADERSMOD_VERSIONS;
import static com.falsepattern.lib.mixin.ITargetedMod.PredicateHelpers.startsWith;

@RequiredArgsConstructor
public enum TargetedMod implements ITargetedMod {
    ARCHAICFIX("ArchaicFix", false, startsWith("archaicfix")),
    FOAMFIX("FoamFix", false, startsWith("foamfix")),
    OPTIFINE_WITHOUT_SHADERS("OptiFine without shaders", false,
                             startsWith("optifine").and(OPTIFINE_SHADERSMOD_VERSIONS.negate())),
    OPTIFINE_WITH_SHADERS("OptiFine with shaders", false, startsWith("optifine").and(OPTIFINE_SHADERSMOD_VERSIONS)),
    FASTCRAFT("FastCraft", false, startsWith("fastcraft")),
    CHROMATICRAFT("ChromatiCraft", false, startsWith("chromaticraft")),
    REDSTONEPASTE("RedstonePaste", false, startsWith("redstonepaste")),
    APPARATUS("Apparatus", false, startsWith("apparatus")),
    RAILCRAFT("Railcraft", true, startsWith("railcraft")),
    ;

    @Getter
    private final String modName;
    @Getter
    private final boolean loadInDevelopment;
    @Getter
    private final Predicate<String> condition;
}
