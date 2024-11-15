/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2024 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
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
import com.falsepattern.falsetweaks.modules.debug.Debug;
import com.falsepattern.lib.mixin.IMixin;
import com.falsepattern.lib.mixin.ITargetedMod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Predicate;

import static com.falsepattern.falsetweaks.config.ThreadingConfig.FAST_SAFETY_CHECKS;
import static com.falsepattern.falsetweaks.config.ThreadingConfig.FAST_THREADED_BLOCK_BOUNDS;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.Mixin.CommonConfigs.BSP;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.Mixin.CommonConfigs.DYNLIGHTS_NONOF;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.Mixin.CommonConfigs.THREADING;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.Mixin.CommonConfigs.TRIANGULATOR;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.Mixin.CommonConfigs.VOXELIZER;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetedMod.AVOID_ANY_OPTIFINE;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetedMod.AVOID_OPTIFINE_WITH_DYNAMIC_LIGHTS;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetedMod.AVOID_OPTIFINE_WITH_SHADERS;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetedMod.NEODYMIUM;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetedMod.REQUIRE_ANY_OPTIFINE;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetedMod.REQUIRE_OPTIFINE_WITH_DYNAMIC_LIGHTS;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetedMod.REQUIRE_OPTIFINE_WITH_SHADERS;
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

    //OptiFine
    Tri_OFTessellatorVanillaMixin(Side.CLIENT,
                                  TRIANGULATOR.and(AVOID_ANY_OPTIFINE),
                                  "triangulator.optifine.TessellatorVanillaMixin"),
    Tri_OFTessellatorVanillaOrOldOptifineMixin(Side.CLIENT,
                                               TRIANGULATOR.and(AVOID_OPTIFINE_WITH_SHADERS),
                                               "triangulator.optifine.TessellatorVanillaOrOldOptifineMixin"),
    Tri_OFTessellatorOptiFineMixin(Side.CLIENT,
                                   TRIANGULATOR.and(REQUIRE_OPTIFINE_WITH_SHADERS),
                                   "triangulator.optifine.TessellatorOptiFineMixin"),

    //RedstonePaste
    Tri_RedstonePasteHighlighterMixin(Side.CLIENT,
                                      TRIANGULATOR.and(require(TargetedMod.REDSTONEPASTE)),
                                      "triangulator.redstonepaste.RedstonePasteHighlighterMixin"),

    //endregion Triangulator Module

    //region Occlusion Tweaks Module

    //For 32chunk render distance without optifine
    Occlusion_PlayerManagerMixin(Side.COMMON, THREADING, "occlusion.PlayerManagerMixin"),

    Occlusion_EntityRendererMixin(Side.CLIENT, THREADING, "occlusion.EntityRendererMixin"),
    Occlusion_GuiVideoSettingsMixin(Side.CLIENT, THREADING, "occlusion.GuiVideoSettingsMixin"),
    Occlusion_RenderGlobalMixin(Side.CLIENT, THREADING, "occlusion.RenderGlobalMixin"),
    Occlusion_WorldRendererMixin(Side.CLIENT, THREADING, "occlusion.WorldRendererMixin"),
    Occlusion_MinecraftMixin(Side.CLIENT, THREADING, "occlusion.MinecraftMixin"),
    Occlusion_GameSettingsMixin(Side.CLIENT,
                                THREADING.and(AVOID_ANY_OPTIFINE),
                                "occlusion.GameSettingsMixin"),
    Occlusion_GameSettingsOptionsMixin(Side.CLIENT,
                                       THREADING.and(AVOID_ANY_OPTIFINE),
                                       "occlusion.GameSettingsOptionsMixin"),
    Occlusion_ClippingHelperMixin(Side.CLIENT,
                                  THREADING,
                                       "occlusion.ClippingHelperMixin"),
    Occlusion_ClippingHelperImplMixin(Side.CLIENT,
                                      THREADING,
                                       "occlusion.ClippingHelperImplMixin"),

    //Neodymium
    Occlusion_Neodymium_NeoRendererMixin(Side.CLIENT,
                                         THREADING.and(require(NEODYMIUM)),
                                         "occlusion.neodymium.NeoRendererMixin"),

    //OptiFine
    Occlusion_Optifine_RenderGlobalMixin(Side.CLIENT,
                                         THREADING.and(REQUIRE_ANY_OPTIFINE),
                                         "occlusion.optifine.RenderGlobalMixin"),
    Occlusion_Optifine_OFGameSettingsOptifineMixin(Side.CLIENT,
                                                   THREADING.and(REQUIRE_ANY_OPTIFINE),
                                                   "occlusion.optifine.GameSettingsOptifineMixin"),
    Occlusion_Optifine_OFGuiVideoSettingsOptifineMixin(Side.CLIENT,
                                                       THREADING.and(REQUIRE_ANY_OPTIFINE),
                                                       "occlusion.optifine.GuiVideoSettingsOptifineMixin"),
    Occlusion_Optifine_WorldRenderer_VanillaMixin(Side.CLIENT,
                                                  THREADING.and(AVOID_ANY_OPTIFINE),
                                                  "occlusion.optifine.WorldRenderer_VanillaMixin"),
    Occlusion_Optifine_WorldRenderer_OFMixin(Side.CLIENT,
                                             THREADING.and(REQUIRE_ANY_OPTIFINE),
                                             "occlusion.optifine.WorldRenderer_OFMixin"),

    //OptiFine with shaders
    Occlusion_Optifine_Shaders_ShadersRendererMixin(Side.CLIENT,
                                                    THREADING.and(REQUIRE_OPTIFINE_WITH_SHADERS),
                                                    "occlusion.optifine.shaders.ShadersRendererMixin"),
    Occlusion_Optifine_Shaders_FrustrumMixin(Side.CLIENT,
                                             THREADING.and(REQUIRE_OPTIFINE_WITH_SHADERS),
                                             "occlusion.optifine.shaders.FrustrumMixin"),


    //FastCraft
    Occlusion_FastCraft_GLAllocationMixin(Side.CLIENT,
                                          THREADING.and(require(TargetedMod.FASTCRAFT)),
                                          "occlusion.fastcraft.GLAllocationMixin"),
    Occlusion_FastCraft_EntityRendererMixin(Side.CLIENT,
                                            THREADING.and(require(TargetedMod.FASTCRAFT)).and(AVOID_ANY_OPTIFINE),
                                            "occlusion.fastcraft.EntityRendererMixin"),

    //Both of them
    Occlusion_OptiFastCraft_RenderGlobalMixin(Side.CLIENT,
                                              THREADING.and(require(TargetedMod.FASTCRAFT).or(REQUIRE_ANY_OPTIFINE)),
                                              "occlusion.optifastcraft.RenderGlobalMixin"),

    //endregion Occlusion Tweaks Module

    //region Threaded Chunk Updates
    ThreadedUpdates_GameSettings(Side.CLIENT, THREADING, "threadedupdates.GameSettingsMixin"),
    ThreadedUpdates_ChunkProviderClientMixin(Side.CLIENT, THREADING, "threadedupdates.ChunkProviderClientMixin"),
    ThreadedUpdates_ForgeHooksClientMixin(Side.CLIENT, THREADING, "threadedupdates.ForgeHooksClientMixin"),
    ThreadedUpdates_RenderBlocksMixin(Side.CLIENT, THREADING, "threadedupdates.RenderBlocksMixin"),
    ThreadedUpdates_RenderGlobalMixin(Side.CLIENT, THREADING, "threadedupdates.RenderGlobalMixin"),
    ThreadedUpdates_TessellatorMixin(Side.CLIENT, THREADING, "threadedupdates.TessellatorMixin"),
    ThreadedUpdates_TessellatorMixin_DebugFast(Side.CLIENT, THREADING.and(condition(() -> FAST_SAFETY_CHECKS)), "threadedupdates.TessellatorMixin_DebugFast"),
    ThreadedUpdates_TessellatorMixin_Debug(Side.CLIENT, THREADING.and(condition(() -> !FAST_SAFETY_CHECKS)), "threadedupdates.TessellatorMixin_Debug"),
    ThreadedUpdates_WorldRenderer_NonOptiFineMixin(Side.CLIENT, THREADING.and(AVOID_ANY_OPTIFINE), "threadedupdates.WorldRenderer_NonOptiFineMixin"),
    ThreadedUpdates_WorldRendererMixin(Side.CLIENT, THREADING, "threadedupdates.WorldRendererMixin"),

    ThreadedUpdates_BlockBounds_BlockMixin_Root(Side.CLIENT, THREADING, "threadedupdates.blockbounds.BlockMixin_Root"),
    ThreadedUpdates_BlockBounds_BlockMixin_Impl(Side.CLIENT, THREADING, "threadedupdates.blockbounds.BlockMixin_Impl"),
    ThreadedUpdates_BlockBounds_BlockMixin_FastImpl(Side.CLIENT, THREADING.and(condition(() -> FAST_THREADED_BLOCK_BOUNDS)), "threadedupdates.blockbounds.BlockMixin_FastImpl"),

    //Neodymium
    ThreadedUpdates_Neodymium_WorldRendererMixin(Side.CLIENT, THREADING.and(require(TargetedMod.NEODYMIUM)), "threadedupdates.neodymium.WorldRendererMixin"),

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
    ThreadedUpdates_OptiFine_MinecraftMixin(Side.CLIENT,
                                            THREADING.and(REQUIRE_OPTIFINE_WITH_SHADERS),
                                            "threadedupdates.optifine.MinecraftMixin"),
    ThreadedUpdates_Optifine_ShadersMixin(Side.CLIENT,
                                          THREADING.and(REQUIRE_OPTIFINE_WITH_SHADERS),
                                          "threadedupdates.optifine.ShadersMixin"),
    ThreadedUpdates_OptiFine_TessellatorMixin(Side.CLIENT,
                                              THREADING.and(REQUIRE_ANY_OPTIFINE),
                                              "threadedupdates.optifine.TessellatorMixin"),
    ThreadedUpdates_OptiFine_WorldClientMixin(Side.CLIENT, THREADING.and(REQUIRE_OPTIFINE_WITH_DYNAMIC_LIGHTS),
                                              "threadedupdates.optifine.WorldClientMixin"),
    ThreadedUpdates_OptiFine_WorldRendererMixin(Side.CLIENT,
                                                THREADING.and(REQUIRE_ANY_OPTIFINE),
                                                "threadedupdates.optifine.WorldRendererMixin"),

    // Nuclear Control 2
    ThreadedUpdates_Nuclear_Control_MainBlockRendererMixin(Side.CLIENT,
                                                           THREADING.and(require(TargetedMod.NUCLEAR_CONTROL)),
                                                           "threadedupdates.nuclearcontrol.MainBlockRendererMixin"),
    ThreadedUpdates_Nuclear_Control_TileEntityInfoPanelRendererMixin(Side.CLIENT,
                                                                     THREADING.and(require(TargetedMod.NUCLEAR_CONTROL)),
                                                                     "threadedupdates.nuclearcontrol.TileEntityInfoPanelRendererMixin"),
    ThreadedUpdates_Nuclear_Control_TileEntityAdvancedInfoPanelMixin(Side.CLIENT,
                                                                     THREADING.and(require(TargetedMod.NUCLEAR_CONTROL)),
                                                                     "threadedupdates.nuclearcontrol.TileEntityAdvancedInfoPanelMixin"),

    // OpenComputers
    ThreadedUpdates_Open_Computers_TileEntityAdvancedInfoPanelMixin(Side.CLIENT,
                                                                    THREADING.and(require(TargetedMod.OPEN_COMPUTERS)),
                                                                    "threadedupdates.opencomputers.RenderStateMixin"),
    // Computronics
    ThreadedUpdates_Computronics_LampRenderMixin(Side.CLIENT,
                                                 THREADING.and(require(TargetedMod.COMPUTRONICS)),
                                                 "threadedupdates.computronics.LampRenderMixin"),
    // Extra Cells 2
    ThreadedUpdates_Extra_Cells_RendererHardMEDriveMixin(Side.CLIENT,
                                                         THREADING.and(require(TargetedMod.EXTRA_CELLS)),
                                                         "threadedupdates.extracells.RendererHardMEDriveMixin"),
    // Automagy
    ThreadedUpdates_Automagy_RenderBlockGlowOverlayMixin(Side.CLIENT,
                                                         THREADING.and(require(TargetedMod.AUTOMAGY)),
                                                         "threadedupdates.automagy.RenderBlockGlowOverlayMixin"),
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

    //region BSP Sorting Module
    BSP_TessellatorMixin(Side.CLIENT,
                         BSP,
                         "bsp.TessellatorMixin"),

    BSP_TessellatorBSPSortingMixin(Side.CLIENT,
                                   BSP.and(avoid(TargetedMod.FOAMFIX)),
                                   "bsp.TessellatorBSPSortingMixin"),

    //FoamFix
    BSP_foamfix_TessellatorBSPSortingMixin(Side.CLIENT,
                                           BSP.and(require(TargetedMod.FOAMFIX)),
                                           "bsp.foamfix.TessellatorBSPSortingMixin"),

    //endregion

    //region Dynamic Lights Module

    DynLights_ItemRendererMixin(Side.CLIENT, DYNLIGHTS_NONOF, "dynlights.ItemRendererMixin"),
    DynLights_RenderGlobalMixin(Side.CLIENT, DYNLIGHTS_NONOF, "dynlights.RenderGlobalMixin"),
    DynLights_WorldClientMixin(Side.CLIENT, DYNLIGHTS_NONOF, "dynlights.WorldClientMixin"),
    DynLights_NonThread_WorldClientMixin(Side.CLIENT, DYNLIGHTS_NONOF.and(THREADING.negate()), "dynlights.nonthread.WorldClientMixin"),
    DynLights_Thread_WorldClientMixin(Side.CLIENT, DYNLIGHTS_NONOF.and(THREADING), "dynlights.thread.WorldClientMixin"),
    DynLights_OF_DynamicLightsMixin(Side.CLIENT, REQUIRE_OPTIFINE_WITH_DYNAMIC_LIGHTS, "dynlights.of.DynamicLightsMixin"),
    //endregion Dynamic Lights Module

    //region Chunk Cache Module
    CC_WorldRendererMixin(Side.CLIENT, condition(() -> ModuleConfig.DYNAMIC_LIGHTS || ModuleConfig.FASTER_CHUNK_CACHE).and(AVOID_OPTIFINE_WITH_DYNAMIC_LIGHTS), "cc.WorldRendererMixin"),
    CC_OF_ChunkCacheOFMixin(Side.CLIENT, REQUIRE_OPTIFINE_WITH_DYNAMIC_LIGHTS, "cc.of.ChunkCacheOFMixin"),
    //endregion Chunk Cache Module

    //region Misc Modules
    ItemRenderList_ItemRendererMixin(Side.CLIENT, condition(() -> ModuleConfig.ITEM_RENDER_LISTS), "misc.ItemRenderList_ItemRendererMixin"),

    MinecartEarBlast_WorldclientMixin(Side.CLIENT, condition(() -> ModuleConfig.MINECART_EAR_BLAST_FIX), "misc.MinecartEarBlast_WorldClientMixin"),

    BeaconFix_TileEntityBeaconRendererMixin(Side.CLIENT, condition(() -> ModuleConfig.BEACON_OPTIMIZATION), "misc.BeaconFix_TileEntityBeaconRendererMixin"),
    BeaconFix_TileEntityBeaconMixin(Side.CLIENT, condition(() -> ModuleConfig.BEACON_OPTIMIZATION), "misc.BeaconFix_TileEntityBeaconMixin"),

    TileEntitySorting_RenderGlobalMixin(Side.CLIENT, condition(() -> ModuleConfig.TE_TRANSPARENCY_FIX), "misc.TileEntitySorting_RenderGlobalMixin"),

    TranslucentBlockLayers_RenderGlobalMixin(Side.CLIENT, condition(() -> ModuleConfig.BLOCK_LAYER_TRANSPARENCY_FIX), "misc.TranslucentBlockLayers_RenderGlobalMixin"),

    SkyFix_RenderGlobalMixin(Side.CLIENT, condition(() -> ModuleConfig.SKY_MESH_OPTIMIZATION), "misc.SkyFix_RenderGlobalMixin"),

    RealmShutUp_GuiMainMenuMixin(Side.CLIENT, condition(() -> ModuleConfig.NO_REALMS_ON_MENU), "misc.RealmShutUp_GuiMainMenuMixin"),
    RealmShutUp_RealmsBridgeMixin(Side.CLIENT, condition(() -> ModuleConfig.NO_REALMS_ON_MENU), "misc.RealmShutUp_RealmsBridgeMixin"),
    //endregion Misc Modules

    //region Particles

    Particles_EntityDiggingFXMixin(Side.CLIENT, condition(() -> ModuleConfig.CUBIC_PARTICLES), "particles.EntityDiggingFXMixin"),
    Particles_EffectRendererMixin(Side.CLIENT, condition(() -> ModuleConfig.CUBIC_PARTICLES), "particles.EffectRendererMixin"),
    //endregion Particles

    //region Debug
    Debug_TileEntityRendererDispatcherMixin(Side.CLIENT, condition(() -> Debug.ENABLED), "debug.TileEntityRendererDispatcherMixin"),
    Debug_Occlusion_Neodymium_NeoRendererMixin(Side.CLIENT, THREADING.and(REQUIRE_OPTIFINE_WITH_SHADERS).and(require(NEODYMIUM)).and(condition(() -> Debug.ENABLED)), "debug.occlusion.neodymium.NeoRendererMixin"),
    Debug_Occlusion_Neodymium_GPUMemoryManagerMixin(Side.CLIENT, THREADING.and(require(NEODYMIUM)).and(condition(() -> Debug.ENABLED)), "debug.occlusion.neodymium.GPUMemoryManagerMixin"),
    //endregion Debug

    //region Render Safety
    RenderingSafety_ForgeHooksClientMixin(Side.CLIENT, condition(() -> ModuleConfig.RENDERING_SAFETY), "rendersafety.ForgeHooksClientMixin"),
    RenderingSafety_ItemRendererMixin(Side.CLIENT, condition(() -> ModuleConfig.RENDERING_SAFETY), "rendersafety.ItemRendererMixin"),
    RenderingSafety_RenderingRegistryMixin(Side.CLIENT, condition(() -> ModuleConfig.RENDERING_SAFETY), "rendersafety.RenderingRegistryMixin"),
    RenderingSafety_TileEntityRendererDispatcherMixin(Side.CLIENT, condition(() -> ModuleConfig.RENDERING_SAFETY).and(avoid(TargetedMod.DRAGONAPI)), "rendersafety.TileEntityRendererDispatcherMixin"),
    RenderingSafety_DragonAPI_TileEntityRenderEventMixin(Side.CLIENT, condition(() -> ModuleConfig.RENDERING_SAFETY).and(require(TargetedMod.DRAGONAPI)), "rendersafety.dragonapi.TileEntityRenderEventMixin"),

    ;
    // @formatter:on

    @Getter
    private final Side side;
    @Getter
    private final Predicate<List<ITargetedMod>> filter;
    @Getter
    private final String mixin;

    public static class CommonConfigs {
        public static final Predicate<List<ITargetedMod>> TRIANGULATOR = condition(ModuleConfig::TRIANGULATOR);
        public static final Predicate<List<ITargetedMod>> THREADING = condition(ModuleConfig::THREADED_CHUNK_UPDATES);
        public static final Predicate<List<ITargetedMod>> BSP = condition(ModuleConfig::BSP_SORTING);
        public static final Predicate<List<ITargetedMod>> VOXELIZER = condition(() -> ModuleConfig.VOXELIZER);
        public static final Predicate<List<ITargetedMod>> DYNLIGHTS_NONOF = condition(() -> ModuleConfig.DYNAMIC_LIGHTS).and(AVOID_OPTIFINE_WITH_DYNAMIC_LIGHTS);
    }
}

