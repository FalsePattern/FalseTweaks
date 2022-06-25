package com.falsepattern.triangulator.mixin.plugin;

import com.falsepattern.lib.mixin.ITargetedMod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

import static com.falsepattern.lib.mixin.ITargetedMod.PredicateHelpers.startsWith;
import static com.falsepattern.triangulator.mixin.plugin.Extras.OPTIFINE_SHADERSMOD_VERSIONS;

@RequiredArgsConstructor
public enum TargetedMod implements ITargetedMod {
    FOAMFIX("FoamFix", false, startsWith("foamfix")),
    OPTIFINE_WITHOUT_SHADERS("OptiFine without shaders", false,
            startsWith("optifine").and(OPTIFINE_SHADERSMOD_VERSIONS.negate())),
    OPTIFINE_WITH_SHADERS("OptiFine with shaders", false,
            startsWith("optifine").and(OPTIFINE_SHADERSMOD_VERSIONS)),
    FASTCRAFT("FastCraft", false, startsWith("fastcraft")),
    REDSTONEPASTE("RedstonePaste", false, startsWith("redstonepaste")),
    ;

    @Getter
    private final String modName;
    @Getter
    private final boolean loadInDevelopment;
    @Getter
    private final Predicate<String> condition;
}
