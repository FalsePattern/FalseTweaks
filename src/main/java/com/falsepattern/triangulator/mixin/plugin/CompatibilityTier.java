package com.falsepattern.triangulator.mixin.plugin;

import lombok.val;

import java.util.*;

/**
 * "Tiers" of compatibility for mixins.
 * Overwrite < InjectCancel < Regular.
 * When the tier of a mixin is lower than the required one, it doesn't get loaded.
 */
public enum CompatibilityTier {
    Overwrite, InjectCancel, Regular;

    public boolean isTierBetterThan(CompatibilityTier tier) {
        return mixinsAllowedForTier.get(tier).contains(this);
    }

    private static final Map<CompatibilityTier, List<CompatibilityTier>> mixinsAllowedForTier = new HashMap<>();

    static {
        addTierCompats(Overwrite, Overwrite, InjectCancel, Regular);
        addTierCompats(InjectCancel, InjectCancel, Regular);
        addTierCompats(Regular, Regular);
    }

    private static void addTierCompats(CompatibilityTier baseTier, CompatibilityTier... compatibles) {
        val list = new ArrayList<CompatibilityTier>(compatibles.length);
        Collections.addAll(list, compatibles);
        mixinsAllowedForTier.put(baseTier, Collections.unmodifiableList(list));
    }
}
