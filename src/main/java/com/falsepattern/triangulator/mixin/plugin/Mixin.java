package com.falsepattern.triangulator.mixin.plugin;

import cpw.mods.fml.relauncher.FMLLaunchHandler;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum Mixin {
    //Vanilla
    VanillaClientQuadComparatorMixin(builder(Side.CLIENT).mixin("vanilla.QuadComparatorMixin")),
    VanillaClientTileEntityBeaconRendererMixin(builder(Side.CLIENT).mixin("vanilla.TileEntityBeaconRendererMixin")),
    VanillaClientRenderBlocksMixin(builder(Side.CLIENT).mixin("vanilla.RenderBlocksMixin")),
    VanillaClientTessellatorMixin(builder(Side.CLIENT).mixin("vanilla.TessellatorMixin")),

    //FoamFix
    FoamFixClientTessellatorVanillaMixin(builder(Side.CLIENT).avoid(TargetedMod.FOAMFIX).mixin("foamfix.TessellatorVanillaMixin")),
    FoamFixClientTessellatorFoamFixMixin(builder(Side.CLIENT).target(TargetedMod.FOAMFIX).mixin("foamfix.TessellatorFoamFixMixin")),

    //OptiFine
    OptiFineClientTessellatorVanillaMixin(builder(Side.CLIENT).avoid(TargetedMod.OPTIFINE).mixin("optifine.TessellatorVanillaMixin")),
    OptiFineClientTessellatorOptiFineMixin(builder(Side.CLIENT).target(TargetedMod.OPTIFINE).mixin("optifine.TessellatorOptiFineMixin"));


    public final String mixin;
    public final Set<TargetedMod> targetedMods;
    public final Set<TargetedMod> avoidedMods;
    private final Side side;

    Mixin(Builder builder) {
        this.mixin = builder.mixin;
        this.targetedMods = Collections.unmodifiableSet(builder.targetedMods);
        this.avoidedMods = Collections.unmodifiableSet(builder.avoidedMods);
        this.side = builder.side;
    }

    public boolean shouldLoad(List<TargetedMod> loadedMods) {
        return (side == Side.COMMON
                || side == Side.SERVER && FMLLaunchHandler.side().isServer()
                || side == Side.CLIENT && FMLLaunchHandler.side().isClient())
               && loadedMods.containsAll(targetedMods) && avoidedMods.stream().noneMatch(loadedMods::contains);
    }


    @SuppressWarnings("SameParameterValue")
    private static Builder builder(Side side) {
        return new Builder(side);
    }

    private static class Builder {
        public String mixin;
        public Side side;
        public Set<TargetedMod> targetedMods = new HashSet<>();
        public final Set<TargetedMod> avoidedMods = new HashSet<>();

        public Builder(Side side) {
            this.side = side;
        }

        public Builder mixin(String mixinClass) {
            mixin = side.name().toLowerCase() + "." + mixinClass;
            return this;
        }

        public Builder target(TargetedMod mod) {
            targetedMods.add(mod);
            return this;
        }

        public Builder avoid(TargetedMod mod) {
            avoidedMods.add(mod);
            return this;
        }
    }

    private enum Side {
        COMMON,
        CLIENT,
        SERVER
    }
}

