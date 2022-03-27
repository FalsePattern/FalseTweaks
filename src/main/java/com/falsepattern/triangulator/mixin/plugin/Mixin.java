package com.falsepattern.triangulator.mixin.plugin;

import cpw.mods.fml.relauncher.FMLLaunchHandler;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

public enum Mixin {
    //BEGIN Minecraft->client
        QuadComparatorMixin(Side.CLIENT, always(),"vanilla.QuadComparatorMixin"),
        TileEntityBeaconRendererMixin(Side.CLIENT, always(),"vanilla.TileEntityBeaconRendererMixin"),
        RenderBlocksMixin(Side.CLIENT, always(),"vanilla.RenderBlocksMixin"),
        TessellatorMixin(Side.CLIENT, always(),"vanilla.TessellatorMixin"),
    //END Minecraft->client
    //BEGIN FoamFix->client
        FFTessellatorVanillaMixin(Side.CLIENT, avoid(TargetedMod.FOAMFIX), "foamfix.TessellatorVanillaMixin"),
        FFTessellatorFoamFixMixin(Side.CLIENT, require(TargetedMod.FOAMFIX), "foamfix.TessellatorFoamFixMixin"),
    //END FoamFix->client
    //BEGIN OptiFine->client
        OFTessellatorVanillaMixin(Side.CLIENT, avoid(TargetedMod.OPTIFINE), "optifine.TessellatorVanillaMixin"),
        OFTessellatorOptiFineMixin(Side.CLIENT, require(TargetedMod.OPTIFINE), "optifine.TessellatorOptiFineMixin"),
    //END OptiFine->client
    //BEGIN RedstonePaste->client
        RedstonePasteHighlighterMixin(Side.CLIENT, require(TargetedMod.REDSTONEPASTE), "redstonepaste.RedstonePasteHighlighterMixin"),
    //END RedstonePaste->client
    ;

    public final Side side;
    public final String mixin;
    public final Predicate<List<TargetedMod>> filter;

    Mixin(Side side, Predicate<List<TargetedMod>> modFilter, String mixin) {
        this.side = side;
        this.mixin = side.name().toLowerCase() + "." + mixin;
        this.filter = modFilter;
    }

    public boolean shouldLoad(List<TargetedMod> loadedMods) {
        return (side == Side.COMMON
                || side == Side.SERVER && FMLLaunchHandler.side().isServer()
                || side == Side.CLIENT && FMLLaunchHandler.side().isClient())
               && filter.test(loadedMods);
    }

    private static Predicate<List<TargetedMod>> never() {
        return (list) -> false;
    }

    private static Predicate<List<TargetedMod>> condition(Supplier<Boolean> condition) {
        return (list) -> condition.get();
    }

    private static Predicate<List<TargetedMod>> always() {
        return (list) -> true;
    }

    private static Predicate<List<TargetedMod>> require(TargetedMod mod) {
        return (list) -> list.contains(mod);
    }

    private static Predicate<List<TargetedMod>> avoid(TargetedMod mod) {
        return (list) -> !list.contains(mod);
    }

    private enum Side {
        COMMON,
        CLIENT,
        SERVER
    }
}

