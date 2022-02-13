package com.falsepattern.triangulator.mixin.plugin;

import com.falsepattern.triangulator.ModInfo;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import lombok.val;
import org.spongepowered.asm.mixin.throwables.MixinException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.falsepattern.triangulator.mixin.plugin.TargetedMod.VANILLA;

public enum Mixin {
    ClientTessellatorMixin(builder(Side.CLIENT).unit(CompatibilityTier.Regular, "TessellatorMixin")),
    ClientQuadComparatorMixin(builder(Side.CLIENT).unit(CompatibilityTier.Regular, "QuadComparatorMixin")),
    ClientTileEntityBeaconRendererMixin(builder(Side.CLIENT).unit(CompatibilityTier.Regular, "TileEntityBeaconRendererMixin")),
    ClientRenderBlocksMixin(builder(Side.CLIENT).unit(CompatibilityTier.Regular, "RenderBlocksMixin"));

    public final MixinUnit[] units;
    public final Set<TargetedMod> targetedMods;
    private final Side side;

    Mixin(Builder builder) {
        this.units = builder.units.toArray(new MixinUnit[0]);
        this.targetedMods = builder.targetedMods;
        this.side = builder.side;
    }

    public boolean shouldLoad(List<TargetedMod> loadedMods) {
        return (side == Side.COMMON
                || side == Side.SERVER && FMLLaunchHandler.side().isServer()
                || side == Side.CLIENT && FMLLaunchHandler.side().isClient())
                && loadedMods.containsAll(targetedMods);
    }

    public String getBestAlternativeForTier(CompatibilityTier tier) {
        for (val unit: units) {
            if (unit.tier.isTierBetterThan(tier)) return unit.mixinClass;
        }
        throw new MixinException("Failed to retrieve mixin alternative for " + this.name() + " in mod " + ModInfo.MODID);
    }


    private static Builder builder(Side side) {
        return new Builder(side).target(VANILLA);
    }

    private static class Builder {
        public final ArrayList<MixinUnit> units = new ArrayList<>();
        public final Side side;
        public final Set<TargetedMod> targetedMods = new HashSet<>();

        public Builder(Side side) {
            this.side = side;
        }

        public Builder unit(CompatibilityTier tier, String mixinClass) {
            units.add(new MixinUnit(tier, side.name().toLowerCase() + "." + mixinClass));
            return this;
        }

        public Builder target(TargetedMod mod) {
            targetedMods.add(mod);
            return this;
        }
    }

    private static class MixinUnit {
        public final CompatibilityTier tier;
        public final String mixinClass;

        public MixinUnit(CompatibilityTier tier, String mixinClass) {
            this.tier = tier;
            this.mixinClass = mixinClass;
        }
    }

    private enum Side {
        COMMON,
        CLIENT,
        SERVER
    }
}

