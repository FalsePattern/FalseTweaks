/*
 * FalseTweaks
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

package com.falsepattern.falsetweaks.mixin.plugin.standard;

import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.falsetweaks.config.TriangulatorConfig;
import com.falsepattern.falsetweaks.modules.leakfix.LeakFixState;
import com.falsepattern.lib.mixin.IMixin;
import com.falsepattern.lib.mixin.ITargetedMod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Predicate;

import static com.falsepattern.lib.mixin.IMixin.PredicateHelpers.avoid;
import static com.falsepattern.lib.mixin.IMixin.PredicateHelpers.condition;
import static com.falsepattern.lib.mixin.IMixin.PredicateHelpers.require;

@RequiredArgsConstructor
public enum Mixin implements IMixin {
    //region Triangulator Module
    Tri_QuadComparatorMixin(Side.CLIENT, condition(() -> ModuleConfig.TRIANGULATOR), "triangulator.QuadComparatorMixin"),
    Tri_RenderBlocksMixin(Side.CLIENT, condition(() -> ModuleConfig.TRIANGULATOR), "triangulator.RenderBlocksUltraMixin"),
    Tri_RenderBlocksCompatMixin(Side.CLIENT,
                                condition(() -> ModuleConfig.TRIANGULATOR)
                                        .and(condition(() -> TriangulatorConfig.RENDER_HOOK_COMPAT_MODE)
                                                     .or(require(TargetedMod.APPARATUS))),
                                "triangulator.RenderBlocksCompatMixin"),
    Tri_RenderBlocksPerformanceMixin(Side.CLIENT,
                                     condition(() -> ModuleConfig.TRIANGULATOR)
                                             .and(condition(() -> !TriangulatorConfig.RENDER_HOOK_COMPAT_MODE)
                                                          .and(avoid(TargetedMod.APPARATUS))),
                                     "triangulator.RenderBlocksPerformanceMixin"),
    Tri_RenderingRegistryMixin(Side.CLIENT,
                               condition(() -> ModuleConfig.TRIANGULATOR),
                               "triangulator.RenderingRegistryMixin"),
    Tri_TessellatorMixin(Side.CLIENT, condition(() -> ModuleConfig.TRIANGULATOR), "triangulator.TessellatorMixin"),
    Tri_WorldRendererMixin(Side.CLIENT, condition(() -> ModuleConfig.TRIANGULATOR), "triangulator.WorldRendererMixin"),

    //FoamFix
    Tri_FFTessellatorVanillaMixin(Side.CLIENT,
                                  condition(() -> ModuleConfig.TRIANGULATOR)
                                          .and(avoid(TargetedMod.FOAMFIX)),
                                  "triangulator.foamfix.TessellatorVanillaMixin"),
    Tri_FFTessellatorFoamFixMixin(Side.CLIENT,
                                  condition(() -> ModuleConfig.TRIANGULATOR)
                                          .and(require(TargetedMod.FOAMFIX)),
                                  "triangulator.foamfix.TessellatorFoamFixMixin"),

    //OptiFine
    Tri_OFTessellatorVanillaMixin(Side.CLIENT,
                                  condition(() -> ModuleConfig.TRIANGULATOR)
                                          .and(avoid(TargetedMod.OPTIFINE_WITHOUT_SHADERS))
                                          .and(avoid(TargetedMod.OPTIFINE_WITH_SHADERS)),
                                  "triangulator.optifine.TessellatorVanillaMixin"),
    Tri_OFTessellatorVanillaOrOldOptifineMixin(Side.CLIENT,
                                               condition(() -> ModuleConfig.TRIANGULATOR)
                                                       .and(avoid(TargetedMod.OPTIFINE_WITH_SHADERS)),
                                               "triangulator.optifine.TessellatorVanillaOrOldOptifineMixin"),
    Tri_OFTessellatorOptiFineMixin(Side.CLIENT,
                                   condition(() -> ModuleConfig.TRIANGULATOR)
                                           .and(require(TargetedMod.OPTIFINE_WITH_SHADERS)),
                                   "triangulator.optifine.TessellatorOptiFineMixin"),

    //ChromatiCraft
    Tri_CCRuneRendererMixin(Side.CLIENT,
                            condition(() -> ModuleConfig.TRIANGULATOR)
                                    .and(require(TargetedMod.CHROMATICRAFT)),
                            "triangulator.chromaticraft.RuneRendererMixin"),

    //RedstonePaste
    Tri_RedstonePasteHighlighterMixin(Side.CLIENT,
                                      condition(() -> ModuleConfig.TRIANGULATOR)
                                              .and(require(TargetedMod.REDSTONEPASTE)),
                                      "triangulator.redstonepaste.RedstonePasteHighlighterMixin"),

    //endregion Triangulator Module

    //region Memory Leak Fix Module
    LeakFix_RenderGlobalMixin(Side.CLIENT, condition(() -> ModuleConfig.MEMORY_LEAK_FIX != LeakFixState.Disable), "leakfix.RenderGlobalMixin"),
    LeakFix_WorldRendererMixin(Side.CLIENT, condition(() -> ModuleConfig.MEMORY_LEAK_FIX != LeakFixState.Disable), "leakfix.WorldRendererMixin"),

    //FastCraft
    LeakFix_FCGLAllocationMixin(Side.CLIENT,
                                condition(() -> ModuleConfig.MEMORY_LEAK_FIX != LeakFixState.Disable)
                                        .and(require(TargetedMod.FASTCRAFT)),
                                "leakfix.fastcraft.GLAllocationMixin"),

    //OptiFine
    LeakFix_OFGameSettingsOptifineMixin(Side.CLIENT,
                                        condition(() -> ModuleConfig.MEMORY_LEAK_FIX != LeakFixState.Disable)
                                                .and(require(TargetedMod.OPTIFINE_WITHOUT_SHADERS)
                                                             .or(require(TargetedMod.OPTIFINE_WITH_SHADERS))),
                                        "leakfix.optifine.GameSettingsOptifineMixin"),
    LeakFix_OFGuiVideoSettingsOptifineMixin(Side.CLIENT,
                                            condition(() -> ModuleConfig.MEMORY_LEAK_FIX != LeakFixState.Disable)
                                                    .and(require(TargetedMod.OPTIFINE_WITHOUT_SHADERS)
                                                                 .or(require(TargetedMod.OPTIFINE_WITH_SHADERS))),
                                            "leakfix.optifine.GuiVideoSettingsOptifineMixin"),
    LeakFix_OFRenderGlobalOptifineMixin(Side.CLIENT,
                                        condition(() -> ModuleConfig.MEMORY_LEAK_FIX != LeakFixState.Disable)
                                                .and(require(TargetedMod.OPTIFINE_WITHOUT_SHADERS)
                                                             .or(require(TargetedMod.OPTIFINE_WITH_SHADERS))),
                                        "leakfix.optifine.RenderGlobalOptiFineMixin"),
    LeakFix_OFWorldRendererVanillaMixin(Side.CLIENT,
                                        condition(() -> ModuleConfig.MEMORY_LEAK_FIX != LeakFixState.Disable)
                                                .and(avoid(TargetedMod.OPTIFINE_WITHOUT_SHADERS)
                                                             .and(avoid(TargetedMod.OPTIFINE_WITH_SHADERS))),
                                        "leakfix.optifine.WorldRendererVanillaMixin"),
    LeakFix_OFWorldRendererOptifineMixin(Side.CLIENT,
                                         condition(() -> ModuleConfig.MEMORY_LEAK_FIX != LeakFixState.Disable)
                                                 .and(require(TargetedMod.OPTIFINE_WITHOUT_SHADERS)
                                                              .or(require(TargetedMod.OPTIFINE_WITH_SHADERS))),
                                         "leakfix.optifine.WorldRendererOptifineMixin"),
    //endregion Memory Leak Fix Module

    //region Texture Optimizations Module
    AnimFix_TextureMapMixin(Side.CLIENT, condition(() -> ModuleConfig.TEXTURE_OPTIMIZATIONS), "animfix.TextureMapMixin"),
    AnimFix_TextureUtilMixin(Side.CLIENT, condition(() -> ModuleConfig.TEXTURE_OPTIMIZATIONS), "animfix.TextureUtilMixin"),
    AnimFix_StitcherMixin(Side.CLIENT, condition(() -> ModuleConfig.TEXTURE_OPTIMIZATIONS), "animfix.StitcherMixin"),
    AnimFix_StitcherSlotMixin(Side.CLIENT, condition(() -> ModuleConfig.TEXTURE_OPTIMIZATIONS), "animfix.StitcherSlotMixin"),

    //FastCraft
    AnimFix_FCAbstractTextureMixin(Side.CLIENT, condition(() -> ModuleConfig.TEXTURE_OPTIMIZATIONS).and(require(TargetedMod.FASTCRAFT)), "animfix.fastcraft.AbstractTextureMixin"),
    AnimFix_FCDynamicTextureMixin(Side.CLIENT, condition(() -> ModuleConfig.TEXTURE_OPTIMIZATIONS).and(require(TargetedMod.FASTCRAFT)), "animfix.fastcraft.DynamicTextureMixin"),
    AnimFix_FCTextureMapMixin(Side.CLIENT, condition(() -> ModuleConfig.TEXTURE_OPTIMIZATIONS).and(require(TargetedMod.FASTCRAFT)), "animfix.fastcraft.TextureMapMixin"),
    AnimFix_FCTextureUtilMixin(Side.CLIENT, condition(() -> ModuleConfig.TEXTURE_OPTIMIZATIONS).and(require(TargetedMod.FASTCRAFT)), "animfix.fastcraft.TextureUtilMixin"),

    //endregion Texture Optimizations Module

    //region Voxelizer Module
    Voxelizer_ItemRendererMixin(Side.CLIENT, condition(() -> ModuleConfig.VOXELIZER), "voxelizer.ItemRendererMixin"),
    Voxelizer_Optifine_ItemRendererMixin(Side.CLIENT, condition(() -> ModuleConfig.VOXELIZER)
            .and(require(TargetedMod.OPTIFINE_WITHOUT_SHADERS).or(require(TargetedMod.OPTIFINE_WITH_SHADERS))), "voxelizer.OFItemRendererMixin"),
    Voxelizer_RenderBlocksMixin(Side.CLIENT, condition(() -> ModuleConfig.VOXELIZER), "voxelizer.RenderBlocksMixin"),
    Voxelizer_RenderItemMixin(Side.CLIENT, condition(() -> ModuleConfig.VOXELIZER), "voxelizer.RenderItemMixin"),
    Voxelizer_TextureAtlasSpriteMixin(Side.CLIENT, condition(() -> ModuleConfig.VOXELIZER), "voxelizer.TextureAtlasSpriteMixin"),
    Voxelizer_TextureManagerMixin(Side.CLIENT, condition(() -> ModuleConfig.VOXELIZER), "voxelizer.TextureManagerMixin"),
    Voxelizer_TextureMapMixin(Side.CLIENT, condition(() -> ModuleConfig.VOXELIZER), "voxelizer.TextureMapMixin"),

    //Railcraft
    Voxelizer_RCRenderTrackMixin(Side.CLIENT, condition(() -> ModuleConfig.VOXELIZER).and(require(TargetedMod.RAILCRAFT)), "voxelizer.railcraft.RenderTrackMixin"),
    //endregion Voxelizer Module

    //region MipMap Fix Module
    MipMapFix_TextureAtlasSpriteMixin(Side.CLIENT, condition(() -> ModuleConfig.MIPMAP_FIX), "mipmapfix.TextureAtlasSpriteMixin"),
    MipMapFix_TextureMapMixin(Side.CLIENT, condition(() -> ModuleConfig.MIPMAP_FIX), "mipmapfix.TextureMapMixin"),
    MipMapFix_TextureUtilMixin(Side.CLIENT,
                               condition(() -> ModuleConfig.MIPMAP_FIX)
                                       .and(avoid(TargetedMod.OPTIFINE_WITHOUT_SHADERS))
                                       .and(avoid(TargetedMod.OPTIFINE_WITH_SHADERS)),
                               "mipmapfix.TextureUtilMixin"),
    //endregion MipMap Fix Module

    //region Misc Modules
    ItemRenderList_ItemRendererMixin(Side.CLIENT, condition(() -> ModuleConfig.ITEM_RENDER_LISTS), "misc.ItemRenderList_ItemRendererMixin"),

    BeaconFix_TileEntityBeaconRendererMixin(Side.CLIENT, condition(() -> ModuleConfig.BEACON_OPTIMIZATION), "misc.BeaconFix_TileEntityBeaconRendererMixin"),
    BeaconFix_TileEntityBeaconMixin(Side.CLIENT, condition(() -> ModuleConfig.BEACON_OPTIMIZATION), "misc.BeaconFix_TileEntityBeaconMixin"),

    TileEntitySorting_RenderGlobalMixin(Side.CLIENT, condition(() -> ModuleConfig.TE_TRANSPARENCY_FIX), "misc.TileEntitySorting_RenderGlobalMixin"),

    //endregion Misc Modules
    ;

    @Getter
    private final Side side;
    @Getter
    private final Predicate<List<ITargetedMod>> filter;
    @Getter
    private final String mixin;
}

