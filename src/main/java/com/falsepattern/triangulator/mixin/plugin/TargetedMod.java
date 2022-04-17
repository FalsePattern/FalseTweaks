package com.falsepattern.triangulator.mixin.plugin;

import com.falsepattern.lib.mixin.ITargetedMod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

import static com.falsepattern.lib.mixin.ITargetedMod.PredicateHelpers.*;

@RequiredArgsConstructor
public enum TargetedMod implements ITargetedMod {
    FOAMFIX("FoamFix", false, startsWith("foamfix")),
    OPTIFINE("OptiFine", false,
            startsWith("optifine")
            .and(    contains("d7")
                 .or(contains("d8"))
                 .or(contains("e3"))
                 .or(contains("e7")))),
    REDSTONEPASTE("RedstonePaste", false, startsWith("redstonepaste")),
    ;

    @Getter
    private final String modName;
    @Getter
    private final boolean loadInDevelopment;
    @Getter
    private final Predicate<String> condition;
}
