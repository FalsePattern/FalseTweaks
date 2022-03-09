package com.falsepattern.triangulator.mixin.plugin;

import cpw.mods.fml.relauncher.FMLLaunchHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum Mixin {
    ClientQuadComparatorMixin(builder(Side.CLIENT).mixin("QuadComparatorMixin")),
    ClientTileEntityBeaconRendererMixin(builder(Side.CLIENT).mixin("TileEntityBeaconRendererMixin")),
    ClientRenderBlocksMixin(builder(Side.CLIENT).mixin("RenderBlocksMixin")),

    ClientTessellatorMixin(builder(Side.CLIENT).mixin("TessellatorMixin")),
    ClientTessellatorVanillaMixin(builder(Side.CLIENT).avoid(TargetedMod.FOAMFIX).mixin("TessellatorVanillaMixin")),
    ClientTessellatorFoamFixMixin(builder(Side.CLIENT).target(TargetedMod.FOAMFIX).mixin("TessellatorFoamFixMixin"));

    public final String mixin;
    public final Set<TargetedMod> targetedMods;
    public final Set<TargetedMod> avoidedMods;
    private final Side side;

    Mixin(Builder builder) {
        this.mixin = builder.mixin;
        this.targetedMods = builder.targetedMods;
        this.avoidedMods = builder.avoidedMods;
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

