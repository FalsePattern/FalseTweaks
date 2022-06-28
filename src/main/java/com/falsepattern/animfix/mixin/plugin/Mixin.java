package com.falsepattern.animfix.mixin.plugin;

import com.falsepattern.lib.mixin.IMixin;
import com.falsepattern.lib.mixin.ITargetedMod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Predicate;

import static com.falsepattern.lib.mixin.IMixin.PredicateHelpers.*;

@RequiredArgsConstructor
public enum Mixin implements IMixin {
    //region Minecraft->client
        TextureMapMixin(Side.CLIENT, always(), "minecraft.TextureMapMixin"),
        TextureUtilMixin(Side.CLIENT, always(), "minecraft.TextureUtilMixin"),
        StitcherMixin(Side.CLIENT, always(), "minecraft.StitcherMixinNew"),
        StitcherSlotMixin(Side.CLIENT, always(), "minecraft.StitcherSlotMixin"),
    //endregion Minecraft->client
    //region FastCraft->client
        FCAbstractTextureMixin(Side.CLIENT, require(TargetedMod.FASTCRAFT), "fastcraft.AbstractTextureMixin"),
        FCDynamicTextureMixin(Side.CLIENT, require(TargetedMod.FASTCRAFT), "fastcraft.DynamicTextureMixin"),
        FCTextureMapMixin(Side.CLIENT, require(TargetedMod.FASTCRAFT), "fastcraft.TextureMapMixin"),
        FCTextureUtilMixin(Side.CLIENT, require(TargetedMod.FASTCRAFT), "fastcraft.TextureUtilMixin"),
    //endregion FastCraft->client
    ;

    @Getter
    private final Side side;
    @Getter
    private final Predicate<List<ITargetedMod>> filter;
    @Getter
    private final String mixin;
}
