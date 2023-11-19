/*
 * This file is part of FalseTweaks.
 *
 * FalseTweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FalseTweaks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalseTweaks. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.falsetweaks.mixin.plugin.standard;

import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.falsetweaks.config.TriangulatorConfig;
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
    // @formatter:off
    //region Triangulator Module
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
    Tri_RenderGlobalMixin(Side.CLIENT, condition(() -> ModuleConfig.TRIANGULATOR), "triangulator.RenderGlobalMixin"),
    Tri_RenderingRegistryMixin(Side.CLIENT,
                               condition(() -> ModuleConfig.TRIANGULATOR),
                               "triangulator.RenderingRegistryMixin"),
    Tri_TessellatorMixin(Side.CLIENT, condition(() -> ModuleConfig.TRIANGULATOR), "triangulator.TessellatorMixin"),
    Tri_WorldRendererMixin(Side.CLIENT, condition(() -> ModuleConfig.TRIANGULATOR), "triangulator.WorldRendererMixin"),

    Tri_BSPSortMixin (Side.CLIENT,
                      condition(() -> ModuleConfig.TRIANGULATOR && ModuleConfig.BSP_SORTING)
                              .and(avoid(TargetedMod.FOAMFIX)),
                      "triangulator.TessellatorBSPSortingMixin"),

    //FoamFix
    Tri_BSPSortFoamFixMixin(Side.CLIENT,
                            condition(() -> ModuleConfig.TRIANGULATOR && ModuleConfig.BSP_SORTING)
                                    .and(require(TargetedMod.FOAMFIX)),
                            "triangulator.foamfix.TessellatorBSPSortingMixin"),

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

    //region Occlusion Tweaks Module
    Occlusion_ChunkMixin(Side.CLIENT, condition(() -> ModuleConfig.OCCLUSION_TWEAKS), "occlusion.ChunkMixin"),
    Occlusion_EntityRendererMixin(Side.CLIENT, condition(() -> ModuleConfig.OCCLUSION_TWEAKS), "occlusion.EntityRendererMixin"),
    Occlusion_GuiVideoSettingsMixin(Side.CLIENT, condition(() -> ModuleConfig.OCCLUSION_TWEAKS), "occlusion.GuiVideoSettingsMixin"),
    Occlusion_RenderGlobalMixin(Side.CLIENT, condition(() -> ModuleConfig.OCCLUSION_TWEAKS), "occlusion.RenderGlobalMixin"),
    Occlusion_WorldRendererMixin(Side.CLIENT, condition(() -> ModuleConfig.OCCLUSION_TWEAKS), "occlusion.WorldRendererMixin"),
    Occlusion_PlayerManagerMixin(Side.CLIENT, condition(() -> ModuleConfig.OCCLUSION_TWEAKS), "occlusion.PlayerManagerMixin"),
    Occlusion_GameSettingsMixin(Side.CLIENT, condition(() -> ModuleConfig.OCCLUSION_TWEAKS).and(avoid(TargetedMod.OPTIFINE_WITHOUT_SHADERS)).and(avoid(TargetedMod.OPTIFINE_WITH_SHADERS)), "occlusion.GameSettingsMixin"),
    Occlusion_GameSettingsOptionsMixin(Side.CLIENT, condition(() -> ModuleConfig.OCCLUSION_TWEAKS).and(avoid(TargetedMod.OPTIFINE_WITHOUT_SHADERS)).and(avoid(TargetedMod.OPTIFINE_WITH_SHADERS)), "occlusion.GameSettingsOptionsMixin"),
    Occluision_Optifine_RenderGlobalMixin(Side.CLIENT, condition(() -> ModuleConfig.OCCLUSION_TWEAKS).and(require(TargetedMod.OPTIFINE_WITHOUT_SHADERS).or(require(TargetedMod.OPTIFINE_WITH_SHADERS))), "occlusion.optifine.RenderGlobalMixin"),
    Occluision_Optifine_ShadersRendererMixin(Side.CLIENT, condition(() -> ModuleConfig.OCCLUSION_TWEAKS).and(require(TargetedMod.OPTIFINE_WITH_SHADERS)), "occlusion.optifine.ShadersRendererMixin"),
    Occluision_Optifine_OFGameSettingsOptifineMixin(Side.CLIENT, condition(() -> ModuleConfig.OCCLUSION_TWEAKS).and(require(TargetedMod.OPTIFINE_WITHOUT_SHADERS).or(require(TargetedMod.OPTIFINE_WITH_SHADERS))), "occlusion.optifine.GameSettingsOptifineMixin"),
    Occluision_Optifine_OFGuiVideoSettingsOptifineMixin(Side.CLIENT, condition(() -> ModuleConfig.OCCLUSION_TWEAKS).and(require(TargetedMod.OPTIFINE_WITHOUT_SHADERS).or(require(TargetedMod.OPTIFINE_WITH_SHADERS))), "occlusion.optifine.GuiVideoSettingsOptifineMixin"),

    //endregion Occlusion Tweaks Module

    //region Threaded Chunk Updates

    ThreadedUpdates_RenderBlocksMixin(Side.CLIENT, condition(() -> ModuleConfig.OCCLUSION_TWEAKS && ModuleConfig.THREADED_CHUNK_UPDATES), "threadedupdates.RenderBlocksMixin"),
    ThreadedUpdates_TessellatorMixin(Side.CLIENT, condition(() -> ModuleConfig.OCCLUSION_TWEAKS && ModuleConfig.THREADED_CHUNK_UPDATES), "threadedupdates.TessellatorMixin"),
    ThreadedUpdates_TessellatorMixin_Debug(Side.CLIENT, condition(() -> ModuleConfig.OCCLUSION_TWEAKS && ModuleConfig.THREADED_CHUNK_UPDATES), "threadedupdates.TessellatorMixin_Debug"),
    ThreadedUpdates_WorldRendererMixin(Side.CLIENT, condition(() -> ModuleConfig.OCCLUSION_TWEAKS && ModuleConfig.THREADED_CHUNK_UPDATES), "threadedupdates.WorldRendererMixin"),
    ThreadedUpdates_Optifine_ShadersMixin(Side.CLIENT, condition(() -> ModuleConfig.OCCLUSION_TWEAKS && ModuleConfig.THREADED_CHUNK_UPDATES).and(require(TargetedMod.OPTIFINE_WITH_SHADERS)), "threadedupdates.optifine.ShadersMixin"),

    //endregion Threaded Chunk Updates

    //region Texture Optimizations Module
    AnimFix_TextureMap_UnprofiledMixin(Side.CLIENT, condition(() -> ModuleConfig.TEXTURE_OPTIMIZATIONS && !ModuleConfig.ADVANCED_PROFILER), "animfix.TextureMap_UnprofiledMixin"),
    AnimFix_TextureMap_ProfiledMixin(Side.CLIENT, condition(() -> ModuleConfig.TEXTURE_OPTIMIZATIONS && ModuleConfig.ADVANCED_PROFILER), "animfix.TextureMap_ProfiledMixin"),
    AnimFix_TextureMap_CommonMixin(Side.CLIENT, condition(() -> ModuleConfig.TEXTURE_OPTIMIZATIONS), "animfix.TextureMap_CommonMixin"),
    AnimFix_TextureUtil_UnprofiledMixin(Side.CLIENT, condition(() -> ModuleConfig.TEXTURE_OPTIMIZATIONS && !ModuleConfig.ADVANCED_PROFILER), "animfix.TextureUtil_UnprofiledMixin"),
    AnimFix_TextureUtil_ProfiledMixin(Side.CLIENT, condition(() -> ModuleConfig.TEXTURE_OPTIMIZATIONS && ModuleConfig.ADVANCED_PROFILER), "animfix.TextureUtil_ProfiledMixin"),
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

    //region Profiler Module
    Profiler_MinecraftMixin(Side.CLIENT, condition(() -> ModuleConfig.ADVANCED_PROFILER), "profiler.MinecraftMixin"),
    Profiler_ProfilerMixin(Side.CLIENT, condition(() -> ModuleConfig.ADVANCED_PROFILER), "profiler.ProfilerMixin"),
    //endregion Profiler Module

    //region Misc Modules
    ItemRenderList_ItemRendererMixin(Side.CLIENT, condition(() -> ModuleConfig.ITEM_RENDER_LISTS), "misc.ItemRenderList_ItemRendererMixin"),

    BeaconFix_TileEntityBeaconRendererMixin(Side.CLIENT, condition(() -> ModuleConfig.BEACON_OPTIMIZATION), "misc.BeaconFix_TileEntityBeaconRendererMixin"),
    BeaconFix_TileEntityBeaconMixin(Side.CLIENT, condition(() -> ModuleConfig.BEACON_OPTIMIZATION), "misc.BeaconFix_TileEntityBeaconMixin"),

    TileEntitySorting_RenderGlobalMixin(Side.CLIENT, condition(() -> ModuleConfig.TE_TRANSPARENCY_FIX), "misc.TileEntitySorting_RenderGlobalMixin"),

    TranslucentBlockLayers_RenderGlobalMixin(Side.CLIENT, condition(() -> ModuleConfig.BLOCK_LAYER_TRANSPARENCY_FIX), "misc.TranslucentBlockLayers_RenderGlobalMixin"),

    SkyFix_RenderGlobalMixin(Side.CLIENT, condition(() -> ModuleConfig.SKY_MESH_OPTIMIZATION), "misc.SkyFix_RenderGlobalMixin"),
    //endregion Misc Modules

    //region Particles

    Particles_EntityDiggingFXMixin(Side.CLIENT, condition(() -> ModuleConfig.CUBIC_PARTICLES), "particles.EntityDiggingFXMixin"),
    Particles_EffectRendererMixin(Side.CLIENT, condition(() -> ModuleConfig.CUBIC_PARTICLES), "particles.EffectRendererMixin"),
    //endregion Particles
    ;
    // @formatter:on

    @Getter
    private final Side side;
    @Getter
    private final Predicate<List<ITargetedMod>> filter;
    @Getter
    private final String mixin;
}

