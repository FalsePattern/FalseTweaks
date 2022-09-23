/*
 * Triangulator
 *
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice, this permission notice and the word "SNEED"
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.triangulator.mixin.plugin;

import com.falsepattern.lib.mixin.IMixin;
import com.falsepattern.lib.mixin.ITargetedMod;
import com.falsepattern.triangulator.config.TriConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Predicate;

import static com.falsepattern.lib.mixin.IMixin.PredicateHelpers.always;
import static com.falsepattern.lib.mixin.IMixin.PredicateHelpers.avoid;
import static com.falsepattern.lib.mixin.IMixin.PredicateHelpers.condition;
import static com.falsepattern.lib.mixin.IMixin.PredicateHelpers.require;

@RequiredArgsConstructor
public enum Mixin implements IMixin {
    //region Minecraft
    ItemRendererMixin(Side.CLIENT, always(), "vanilla.ItemRendererMixin"),
    QuadComparatorMixin(Side.CLIENT, always(), "vanilla.QuadComparatorMixin"),
    TileEntityBeaconRendererMixin(Side.CLIENT, always(), "vanilla.TileEntityBeaconRendererMixin"),
    TileEntityBeaconMixin(Side.CLIENT, always(), "vanilla.TileEntityBeaconMixin"),
    RenderBlocksMixin(Side.CLIENT, always(), "vanilla.RenderBlocksUltraMixin"),
    RenderBlocksCompatMixin(Side.CLIENT, condition(() -> TriConfig.RENDER_HOOK_COMPAT_MODE), "vanilla.RenderBlocksCompatMixin"),
    RenderBlocksPerformanceMixin(Side.CLIENT, condition(() -> !TriConfig.RENDER_HOOK_COMPAT_MODE), "vanilla.RenderBlocksPerformanceMixin"),
    RenderGlobalMixin(Side.CLIENT, always(), "vanilla.RenderGlobalMixin"),
    TessellatorMixin(Side.CLIENT, always(), "vanilla.TessellatorMixin"),
    //region leak fix
    LeakFixRenderGlobalMixin(Side.CLIENT, always(), "vanilla.leakfix.RenderGlobalMixin"),
    LeakFixWorldRendererMixin(Side.CLIENT, always(), "vanilla.leakfix.WorldRendererMixin"),
    //endregion leak fix
    //endregion Minecraft
    //region FoamFix
    FFTessellatorVanillaMixin(Side.CLIENT, avoid(TargetedMod.FOAMFIX), "foamfix.TessellatorVanillaMixin"),
    FFTessellatorFoamFixMixin(Side.CLIENT, require(TargetedMod.FOAMFIX), "foamfix.TessellatorFoamFixMixin"),
    //endregion FoamFix
    //region OptiFine
    OFTessellatorVanillaMixin(Side.CLIENT,
                              avoid(TargetedMod.OPTIFINE_WITHOUT_SHADERS).and(avoid(TargetedMod.OPTIFINE_WITH_SHADERS)),
                              "optifine.TessellatorVanillaMixin"),
    OFTessellatorVanillaOrOldOptifineMixin(Side.CLIENT, avoid(TargetedMod.OPTIFINE_WITH_SHADERS),
                                           "optifine.TessellatorVanillaOrOldOptifineMixin"),
    OFTessellatorOptiFineMixin(Side.CLIENT, require(TargetedMod.OPTIFINE_WITH_SHADERS),
                               "optifine.TessellatorOptiFineMixin"),
    //region leak fix
    OFGameSettingsOptifineMixin(Side.CLIENT, require(TargetedMod.OPTIFINE_WITHOUT_SHADERS).or(
            require(TargetedMod.OPTIFINE_WITH_SHADERS)), "optifine.leakfix.GameSettingsOptifineMixin"),
    OFGuiVideoSettingsOptifineMixin(Side.CLIENT, require(TargetedMod.OPTIFINE_WITHOUT_SHADERS).or(
            require(TargetedMod.OPTIFINE_WITH_SHADERS)), "optifine.leakfix.GuiVideoSettingsOptifineMixin"),
    OFRenderGlobalOptifineMixin(Side.CLIENT, require(TargetedMod.OPTIFINE_WITHOUT_SHADERS).or(
            require(TargetedMod.OPTIFINE_WITH_SHADERS)), "optifine.leakfix.RenderGlobalOptiFineMixin"),
    OFWorldRendererVanillaMixin(Side.CLIENT, avoid(TargetedMod.OPTIFINE_WITHOUT_SHADERS).and(
            avoid(TargetedMod.OPTIFINE_WITH_SHADERS)), "optifine.leakfix.WorldRendererVanillaMixin"),
    OFWorldRendererOptifineMixin(Side.CLIENT, require(TargetedMod.OPTIFINE_WITHOUT_SHADERS).or(
            require(TargetedMod.OPTIFINE_WITH_SHADERS)), "optifine.leakfix.WorldRendererOptifineMixin"),
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
    RedstonePasteHighlighterMixin(Side.CLIENT, require(TargetedMod.REDSTONEPASTE),
                                  "redstonepaste.RedstonePasteHighlighterMixin"),
    //endregion RedstonePaste

    //region Minecraft->client
    TextureMapMixin(Side.CLIENT, always(), "animfix.minecraft.TextureMapMixin"),
    TextureUtilMixin(Side.CLIENT, always(), "animfix.minecraft.TextureUtilMixin"),
    StitcherMixin(Side.CLIENT, always(), "animfix.minecraft.StitcherMixin"),
    StitcherSlotMixin(Side.CLIENT, always(), "animfix.minecraft.StitcherSlotMixin"),
    //endregion Minecraft->client
    //region FastCraft->client
    FCAbstractTextureMixin(Side.CLIENT, require(TargetedMod.FASTCRAFT), "animfix.fastcraft.AbstractTextureMixin"),
    FCDynamicTextureMixin(Side.CLIENT, require(TargetedMod.FASTCRAFT), "animfix.fastcraft.DynamicTextureMixin"),
    FCTextureMapMixin(Side.CLIENT, require(TargetedMod.FASTCRAFT), "animfix.fastcraft.TextureMapMixin"),
    FCTextureUtilMixin(Side.CLIENT, require(TargetedMod.FASTCRAFT), "animfix.fastcraft.TextureUtilMixin"),
    //endregion FastCraft->client
    ;

    @Getter
    private final Side side;
    @Getter
    private final Predicate<List<ITargetedMod>> filter;
    @Getter
    private final String mixin;
}

