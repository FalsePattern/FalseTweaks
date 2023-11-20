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

import static com.falsepattern.falsetweaks.mixin.plugin.standard.Mixin.CommonConfigs.BSP;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.Mixin.CommonConfigs.OCCLUSION;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.Mixin.CommonConfigs.THREADING;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.Mixin.CommonConfigs.TRIANGULATOR;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.Mixin.CommonConfigs.VOXELIZER;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetedMod.AVOID_ANY_OPTIFINE;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetedMod.REQUIRE_ANY_OPTIFINE;
import static com.falsepattern.lib.mixin.IMixin.PredicateHelpers.avoid;
import static com.falsepattern.lib.mixin.IMixin.PredicateHelpers.condition;
import static com.falsepattern.lib.mixin.IMixin.PredicateHelpers.require;

@RequiredArgsConstructor
public enum Mixin implements IMixin {
    // @formatter:off
    //region Triangulator Module
    Tri_RenderBlocksMixin(Side.CLIENT, TRIANGULATOR, "triangulator.RenderBlocksUltraMixin"),
    Tri_RenderBlocksCompatMixin(Side.CLIENT, TRIANGULATOR.and(condition(() -> TriangulatorConfig.RENDER_HOOK_COMPAT_MODE)
                                                                      .or(require(TargetedMod.APPARATUS))),
                                "triangulator.RenderBlocksCompatMixin"),
    Tri_RenderBlocksPerformanceMixin(Side.CLIENT, TRIANGULATOR.and(condition(() -> !TriangulatorConfig.RENDER_HOOK_COMPAT_MODE)
                                                                           .and(avoid(TargetedMod.APPARATUS))),
                                     "triangulator.RenderBlocksPerformanceMixin"),
    Tri_RenderGlobalMixin(Side.CLIENT, TRIANGULATOR, "triangulator.RenderGlobalMixin"),
    Tri_RenderingRegistryMixin(Side.CLIENT, TRIANGULATOR, "triangulator.RenderingRegistryMixin"),
    Tri_TessellatorMixin(Side.CLIENT, TRIANGULATOR, "triangulator.TessellatorMixin"),
    Tri_WorldRendererMixin(Side.CLIENT, TRIANGULATOR, "triangulator.WorldRendererMixin"),

    Tri_BSPSortMixin(Side.CLIENT,
                     BSP.and(avoid(TargetedMod.FOAMFIX)),
                     "triangulator.TessellatorBSPSortingMixin"),

    //FoamFix
    Tri_BSPSortFoamFixMixin(Side.CLIENT,
                            BSP.and(require(TargetedMod.FOAMFIX)),
                            "triangulator.foamfix.TessellatorBSPSortingMixin"),

    //OptiFine
    Tri_OFTessellatorVanillaMixin(Side.CLIENT,
                                  TRIANGULATOR.and(AVOID_ANY_OPTIFINE),
                                  "triangulator.optifine.TessellatorVanillaMixin"),
    Tri_OFTessellatorVanillaOrOldOptifineMixin(Side.CLIENT,
                                               TRIANGULATOR.and(avoid(TargetedMod.OPTIFINE_WITH_SHADERS)),
                                               "triangulator.optifine.TessellatorVanillaOrOldOptifineMixin"),
    Tri_OFTessellatorOptiFineMixin(Side.CLIENT,
                                   TRIANGULATOR.and(require(TargetedMod.OPTIFINE_WITH_SHADERS)),
                                   "triangulator.optifine.TessellatorOptiFineMixin"),

    //ChromatiCraft
    Tri_CCRuneRendererMixin(Side.CLIENT,
                            TRIANGULATOR.and(require(TargetedMod.CHROMATICRAFT)),
                            "triangulator.chromaticraft.RuneRendererMixin"),

    //RedstonePaste
    Tri_RedstonePasteHighlighterMixin(Side.CLIENT,
                                      TRIANGULATOR.and(require(TargetedMod.REDSTONEPASTE)),
                                      "triangulator.redstonepaste.RedstonePasteHighlighterMixin"),

    //endregion Triangulator Module

    //region Occlusion Tweaks Module

    //For 32chunk render distance without optifine
    Occlusion_PlayerManagerMixin(Side.COMMON, OCCLUSION, "occlusion.PlayerManagerMixin"),

    Occlusion_ChunkMixin(Side.CLIENT, OCCLUSION, "occlusion.ChunkMixin"),
    Occlusion_EntityRendererMixin(Side.CLIENT, OCCLUSION, "occlusion.EntityRendererMixin"),
    Occlusion_GuiVideoSettingsMixin(Side.CLIENT, OCCLUSION, "occlusion.GuiVideoSettingsMixin"),
    Occlusion_RenderGlobalMixin(Side.CLIENT, OCCLUSION, "occlusion.RenderGlobalMixin"),
    Occlusion_WorldRendererMixin(Side.CLIENT, OCCLUSION, "occlusion.WorldRendererMixin"),
    Occlusion_GameSettingsMixin(Side.CLIENT,
                                OCCLUSION.and(AVOID_ANY_OPTIFINE),
                                "occlusion.GameSettingsMixin"),
    Occlusion_GameSettingsOptionsMixin(Side.CLIENT,
                                       OCCLUSION.and(AVOID_ANY_OPTIFINE),
                                       "occlusion.GameSettingsOptionsMixin"),

    //OptiFine
    Occlusion_Optifine_RenderGlobalMixin(Side.CLIENT,
                                         OCCLUSION.and(REQUIRE_ANY_OPTIFINE),
                                         "occlusion.optifine.RenderGlobalMixin"),
    Occlusion_Optifine_ShadersRendererMixin(Side.CLIENT,
                                            OCCLUSION.and(require(TargetedMod.OPTIFINE_WITH_SHADERS)),
                                            "occlusion.optifine.ShadersRendererMixin"),
    Occlusion_Optifine_OFGameSettingsOptifineMixin(Side.CLIENT,
                                                   OCCLUSION.and(REQUIRE_ANY_OPTIFINE),
                                                   "occlusion.optifine.GameSettingsOptifineMixin"),
    Occlusion_Optifine_OFGuiVideoSettingsOptifineMixin(Side.CLIENT,
                                                       OCCLUSION.and(REQUIRE_ANY_OPTIFINE),
                                                       "occlusion.optifine.GuiVideoSettingsOptifineMixin"),
    Occlusion_Optifine_WorldRenderer_VanillaMixin(Side.CLIENT,
                                                  OCCLUSION.and(AVOID_ANY_OPTIFINE),
                                                  "occlusion.optifine.WorldRenderer_VanillaMixin"),
    Occlusion_Optifine_WorldRenderer_OFMixin(Side.CLIENT,
                                             OCCLUSION.and(REQUIRE_ANY_OPTIFINE),
                                             "occlusion.optifine.WorldRenderer_OFMixin"),

    //FastCraft
    Occlusion_FastCraft_GLAllocationMixin(Side.CLIENT,
                                          OCCLUSION.and(require(TargetedMod.FASTCRAFT)),
                                          "occlusion.fastcraft.GLAllocationMixin"),
    Occlusion_FastCraft_EntityRendererMixin(Side.CLIENT,
                                            OCCLUSION.and(require(TargetedMod.FASTCRAFT)).and(AVOID_ANY_OPTIFINE),
                                            "occlusion.fastcraft.EntityRendererMixin"),

    //Both of them
    Occlusion_OptiFastCraft_RenderGlobalMixin(Side.CLIENT,
                                              OCCLUSION.and(require(TargetedMod.FASTCRAFT).or(REQUIRE_ANY_OPTIFINE)),
                                              "occlusion.optifastcraft.RenderGlobalMixin"),

    //endregion Occlusion Tweaks Module

    //region Threaded Chunk Updates

    ThreadedUpdates_ChunkProviderClientMixin(Side.CLIENT, THREADING, "threadedupdates.ChunkProviderClientMixin"),
    ThreadedUpdates_RenderBlocksMixin(Side.CLIENT, THREADING, "threadedupdates.RenderBlocksMixin"),
    ThreadedUpdates_TessellatorMixin(Side.CLIENT, THREADING, "threadedupdates.TessellatorMixin"),
    ThreadedUpdates_TessellatorMixin_Debug(Side.CLIENT, THREADING, "threadedupdates.TessellatorMixin_Debug"),
    ThreadedUpdates_WorldRendererMixin(Side.CLIENT, THREADING, "threadedupdates.WorldRendererMixin"),
    ThreadedUpdates_Optifine_ShadersMixin(Side.CLIENT,
                                          THREADING.and(require(TargetedMod.OPTIFINE_WITH_SHADERS)),
                                          "threadedupdates.optifine.ShadersMixin"),

    //DragonAPI
    ThreadedUpdates_DragonAPI_WorldRenderer_VanillaMixin(Side.CLIENT,
                                                         THREADING.and(avoid(TargetedMod.DRAGONAPI)),
                                                         "threadedupdates.dragonapi.WorldRenderer_VanillaMixin"),
    ThreadedUpdates_DragonAPI_WorldRenderer_DAPIMixin(Side.CLIENT,
                                                      THREADING.and(require(TargetedMod.DRAGONAPI)),
                                                      "threadedupdates.dragonapi.WorldRenderer_DAPIMixin"),

    //OptiFine
    ThreadedUpdates_OptiFine_GameSettingsMixin(Side.CLIENT,
                                               THREADING.and(REQUIRE_ANY_OPTIFINE),
                                               "threadedupdates.optifine.GameSettingsMixin"),
    ThreadedUpdates_OptiFine_GuiPerformanceSettingsOFMixin(Side.CLIENT,
                                               THREADING.and(REQUIRE_ANY_OPTIFINE),
                                               "threadedupdates.optifine.GuiPerformanceSettingsOFMixin"),

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
    Voxelizer_ItemRendererMixin(Side.CLIENT, VOXELIZER, "voxelizer.ItemRendererMixin"),
    Voxelizer_Optifine_ItemRendererMixin(Side.CLIENT,
                                         VOXELIZER.and(REQUIRE_ANY_OPTIFINE),
                                         "voxelizer.OFItemRendererMixin"),
    Voxelizer_RenderBlocksMixin(Side.CLIENT, VOXELIZER, "voxelizer.RenderBlocksMixin"),
    Voxelizer_RenderItemMixin(Side.CLIENT, VOXELIZER, "voxelizer.RenderItemMixin"),
    Voxelizer_TextureAtlasSpriteMixin(Side.CLIENT, VOXELIZER, "voxelizer.TextureAtlasSpriteMixin"),
    Voxelizer_TextureManagerMixin(Side.CLIENT, VOXELIZER, "voxelizer.TextureManagerMixin"),
    Voxelizer_TextureMapMixin(Side.CLIENT, VOXELIZER, "voxelizer.TextureMapMixin"),

    //Railcraft
    Voxelizer_RCRenderTrackMixin(Side.CLIENT,
                                 VOXELIZER.and(require(TargetedMod.RAILCRAFT)),
                                 "voxelizer.railcraft.RenderTrackMixin"),
    //endregion Voxelizer Module

    //region MipMap Fix Module
    MipMapFix_TextureAtlasSpriteMixin(Side.CLIENT, condition(() -> ModuleConfig.MIPMAP_FIX), "mipmapfix.TextureAtlasSpriteMixin"),
    MipMapFix_TextureMapMixin(Side.CLIENT, condition(() -> ModuleConfig.MIPMAP_FIX), "mipmapfix.TextureMapMixin"),
    MipMapFix_TextureUtilMixin(Side.CLIENT,
                               condition(() -> ModuleConfig.MIPMAP_FIX)
                                       .and(AVOID_ANY_OPTIFINE),
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

    public static class CommonConfigs {
        public static final Predicate<List<ITargetedMod>> TRIANGULATOR = condition(() -> ModuleConfig.TRIANGULATOR);
        public static final Predicate<List<ITargetedMod>> OCCLUSION = condition(() -> ModuleConfig.OCCLUSION_TWEAKS);
        public static final Predicate<List<ITargetedMod>> THREADING = condition(ModuleConfig::THREADED_CHUNK_UPDATES);
        public static final Predicate<List<ITargetedMod>> BSP = condition(() -> ModuleConfig.TRIANGULATOR && ModuleConfig.BSP_SORTING);
        public static final Predicate<List<ITargetedMod>> VOXELIZER = condition(() -> ModuleConfig.VOXELIZER);
    }

    @Getter
    private final Side side;
    @Getter
    private final Predicate<List<ITargetedMod>> filter;
    @Getter
    private final String mixin;
}

