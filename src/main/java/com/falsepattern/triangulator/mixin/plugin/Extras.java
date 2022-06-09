package com.falsepattern.triangulator.mixin.plugin;

import java.util.function.Predicate;

import static com.falsepattern.lib.mixin.ITargetedMod.PredicateHelpers.contains;

class Extras {
    static final Predicate<String> OPTIFINE_SHADERSMOD_VERSIONS =
            contains("d7")
            .or(contains("d8"))
            .or(contains("e3"))
            .or(contains("e7"));
}
