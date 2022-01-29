package com.falsepattern.animfix.mixinplugin;

import cpw.mods.fml.relauncher.FMLLaunchHandler;

import java.util.*;

public enum Mixin {
    ClientTextureMapMixin(builder(Side.CLIENT).mixin("TextureMapMixin")),
    ClientTextureUtilMixin(builder(Side.CLIENT).mixin("TextureUtilMixin")),
    ClientStitcherMixin(builder(Side.CLIENT).mixin("StitcherMixin")),
    ClientStitcherSlotMixin(builder(Side.CLIENT).mixin("StitcherSlotMixin"))
    ;

    public final String mixin;
    public final Set<TargetedMod> targetedMods;
    private final Side side;

    Mixin(Builder builder) {
        this.mixin = builder.mixin;
        this.targetedMods = builder.targetedMods;
        this.side = builder.side;
    }

    public boolean shouldLoad(List<TargetedMod> loadedMods) {
        return (side == Side.COMMON
                || side == Side.SERVER && FMLLaunchHandler.side().isServer()
                || side == Side.CLIENT && FMLLaunchHandler.side().isClient())
                && loadedMods.containsAll(targetedMods);
    }


    @SuppressWarnings("SameParameterValue")
    private static Builder builder(Side side) {
        return new Builder(side);
    }

    private static class Builder {
        public String mixin;
        public Side side;
        public Set<TargetedMod> targetedMods = new HashSet<>();

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
    }

    private enum Side {
        COMMON,
        CLIENT,
        SERVER
    }
}

