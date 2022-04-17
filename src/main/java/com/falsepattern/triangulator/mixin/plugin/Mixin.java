package com.falsepattern.triangulator.mixin.plugin;

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
        QuadComparatorMixin(Side.CLIENT, always(),"vanilla.QuadComparatorMixin"),
        TileEntityBeaconRendererMixin(Side.CLIENT, always(),"vanilla.TileEntityBeaconRendererMixin"),
        RenderBlocksMixin(Side.CLIENT, always(),"vanilla.RenderBlocksMixin"),
        TessellatorMixin(Side.CLIENT, always(),"vanilla.TessellatorMixin"),
    //endregion Minecraft->client
    //region FoamFix->client
        FFTessellatorVanillaMixin(Side.CLIENT, avoid(TargetedMod.FOAMFIX), "foamfix.TessellatorVanillaMixin"),
        FFTessellatorFoamFixMixin(Side.CLIENT, require(TargetedMod.FOAMFIX), "foamfix.TessellatorFoamFixMixin"),
    //endregion FoamFix->client
    //region OptiFine->client
        OFTessellatorVanillaMixin(Side.CLIENT, avoid(TargetedMod.OPTIFINE), "optifine.TessellatorVanillaMixin"),
        OFTessellatorOptiFineMixin(Side.CLIENT, require(TargetedMod.OPTIFINE), "optifine.TessellatorOptiFineMixin"),
    //endregion OptiFine->client
    //region RedstonePaste->client
        RedstonePasteHighlighterMixin(Side.CLIENT, require(TargetedMod.REDSTONEPASTE), "redstonepaste.RedstonePasteHighlighterMixin"),
    //endregion RedstonePaste->client
    ;

    @Getter
    private final Side side;
    @Getter
    private final Predicate<List<ITargetedMod>> filter;
    @Getter
    private final String mixin;
}

