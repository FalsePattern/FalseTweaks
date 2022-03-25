package com.falsepattern.animfix.mixin.plugin;

import cpw.mods.fml.relauncher.FMLLaunchHandler;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;


public enum Mixin {
    //BEGIN Minecraft->client
        TextureMapMixin(Side.CLIENT, always(), "minecraft.TextureMapMixin"),
        TextureUtilMixin(Side.CLIENT, always(), "minecraft.TextureUtilMixin"),
        StitcherMixin(Side.CLIENT, always(), "minecraft.StitcherMixin"),
        StitcherSlotMixin(Side.CLIENT, always(), "minecraft.StitcherSlotMixin"),
    //END Minecraft->client
    //BEGIN FastCraft->client
        FCAbstractTextureMixin(Side.CLIENT, always(), "fastcraft.AbstractTextureMixin"),
        FCDynamicTextureMixin(Side.CLIENT, always(), "fastcraft.DynamicTextureMixin"),
        FCTextureMapMixin(Side.CLIENT, always(), "fastcraft.TextureMapMixin"),
        FCTextureUtilMixin(Side.CLIENT, always(), "fastcraft.TextureUtilMixin"),
    //END FastCraft->client
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
