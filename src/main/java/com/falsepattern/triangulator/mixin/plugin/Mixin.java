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
    //region Minecraft
    ItemRendererMixin(Side.CLIENT, always(), "vanilla.ItemRendererMixin"),
    QuadComparatorMixin(Side.CLIENT, always(), "vanilla.QuadComparatorMixin"),
    TileEntityBeaconRendererMixin(Side.CLIENT, always(), "vanilla.TileEntityBeaconRendererMixin"),
    RenderBlocksMixin(Side.CLIENT, always(), "vanilla.RenderBlocksMixin"),
    TessellatorMixin(Side.CLIENT, always(), "vanilla.TessellatorMixin"),
    //region leak fix
    RenderGlobalMixin(Side.CLIENT, always(), "vanilla.leakfix.RenderGlobalMixin"),
    WorldRendererMixin(Side.CLIENT, always(), "vanilla.leakfix.WorldRendererMixin"),
    //endregion leak fix
    //endregion Minecraft
    //region FoamFix
    FFTessellatorVanillaMixin(Side.CLIENT, avoid(TargetedMod.FOAMFIX), "foamfix.TessellatorVanillaMixin"),
    FFTessellatorFoamFixMixin(Side.CLIENT, require(TargetedMod.FOAMFIX), "foamfix.TessellatorFoamFixMixin"),
    //endregion FoamFix
    //region OptiFine
    OFGameSettingsOptifineMixin(Side.CLIENT, require(TargetedMod.OPTIFINE_WITHOUT_SHADERS).or(require(TargetedMod.OPTIFINE_WITH_SHADERS)), "optifine.GameSettingsOptifineMixin"),
    OFGuiVideoSettingsOptifineMixin(Side.CLIENT, require(TargetedMod.OPTIFINE_WITHOUT_SHADERS).or(require(TargetedMod.OPTIFINE_WITH_SHADERS)), "optifine.GuiVideoSettingsOptifineMixin"),
    OFTessellatorVanillaMixin(Side.CLIENT, avoid(TargetedMod.OPTIFINE_WITHOUT_SHADERS).and(avoid(TargetedMod.OPTIFINE_WITH_SHADERS)), "optifine.TessellatorVanillaMixin"),
    OFTessellatorVanillaOrOldOptifineMixin(Side.CLIENT, avoid(TargetedMod.OPTIFINE_WITH_SHADERS), "optifine.TessellatorVanillaOrOldOptifineMixin"),
    OFTessellatorOptiFineMixin(Side.CLIENT, require(TargetedMod.OPTIFINE_WITH_SHADERS), "optifine.TessellatorOptiFineMixin"),
    //region leak fix
    OFRenderGlobalOptifineMixin(Side.CLIENT, require(TargetedMod.OPTIFINE_WITHOUT_SHADERS).or(require(TargetedMod.OPTIFINE_WITH_SHADERS)), "optifine.leakfix.RenderGlobalOptiFineMixin"),
    OFWorldRendererVanillaMixin(Side.CLIENT, avoid(TargetedMod.OPTIFINE_WITHOUT_SHADERS).and(avoid(TargetedMod.OPTIFINE_WITH_SHADERS)), "optifine.leakfix.WorldRendererVanillaMixin"),
    OFWorldRendererOptifineMixin(Side.CLIENT, require(TargetedMod.OPTIFINE_WITHOUT_SHADERS).or(require(TargetedMod.OPTIFINE_WITH_SHADERS)), "optifine.leakfix.WorldRendererOptifineMixin"),
    //endregion leak fix
    //endregion OptiFine
    //region FastCraft
    //region leak fix
    GLAllocatorMixin(Side.CLIENT, require(TargetedMod.FASTCRAFT), "fastcraft.leakfix.GLAllocationMixin"),
    //endregion leak fix
    //endregion FastCraft
    //region ChromatiCraft
    CCRuneRendererMixin(Side.CLIENT, require(TargetedMod.CHROMATICRAFT), "chromaticraft.RuneRendererMixin"),
    //endregion ChromatiCraft
    //region RedstonePaste
    RedstonePasteHighlighterMixin(Side.CLIENT, require(TargetedMod.REDSTONEPASTE), "redstonepaste.RedstonePasteHighlighterMixin"),
    //endregion RedstonePaste
    ;

    @Getter
    private final Side side;
    @Getter
    private final Predicate<List<ITargetedMod>> filter;
    @Getter
    private final String mixin;
}

