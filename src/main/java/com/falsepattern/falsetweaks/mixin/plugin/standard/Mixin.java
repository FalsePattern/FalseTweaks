/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2025 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalseTweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
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

import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.falsetweaks.config.AOFixConfig;
import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.falsetweaks.config.ThreadingConfig;
import com.falsepattern.falsetweaks.modules.debug.Debug;
import com.falsepattern.lib.mixin.v2.MixinHelper;
import com.falsepattern.lib.mixin.v2.SidedMixins;
import com.falsepattern.lib.mixin.v2.TaggedMod;
import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.intellij.lang.annotations.Language;

import java.util.function.BooleanSupplier;

import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetMod.Automagy;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetMod.Computronics;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetMod.CoreTweaks;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetMod.DragonAPI;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetMod.ExtraCells;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetMod.FastCraft;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetMod.FoamFix;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetMod.LittleTiles;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetMod.Malisis;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetMod.Malisis_NH;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetMod.NotFine;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetMod.NuclearControl;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetMod.OpenComputers;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetMod.OptiFine;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetMod.OptiFineDynamicLights;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetMod.OptiFineShadersMod;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetMod.RailCraft;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetMod.RedstonePaste;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetMod.SecurityCraft;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetMod.StorageDrawers;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetMod.StorageDrawers_ThreadSafe;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetMod.SwanSong;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetMod.Techguns;
import static com.falsepattern.falsetweaks.mixin.plugin.standard.TargetMod.ThermalExpansion;
import static com.falsepattern.lib.mixin.v2.MixinHelper.avoid;
import static com.falsepattern.lib.mixin.v2.MixinHelper.builder;
import static com.falsepattern.lib.mixin.v2.MixinHelper.mods;
import static com.falsepattern.lib.mixin.v2.MixinHelper.require;

@SuppressWarnings("UnstableApiUsage")
@RequiredArgsConstructor
public enum Mixin implements IMixins {
    // @formatter:off
    Core(Phase.EARLY,
         client("core.TessellatorMixin",
                "core.WorldRendererMixin")),

    VertexAPI(Phase.EARLY,
              client("vertexapi.TessellatorMixin",
                     "vertexapi.QuadComparatorMixin")),
    VertexAPI_NoFoamFix(Phase.EARLY,
                        () -> !ModuleConfig.BSP_SORTING,
                        mods(avoid(FoamFix), avoid(SwanSong)),
                        client("vertexapi.TessellatorMixin_AvoidFoamFix")),
    VertexAPI_FoamFix(Phase.EARLY,
                      () -> !ModuleConfig.BSP_SORTING,
                      mods(require(FoamFix), avoid(SwanSong)),
                      client("vertexapi.TessellatorMixin_RequireFoamFix")),
    VertexAPI_Swansong(Phase.EARLY,
                       require(SwanSong),
                       client("vertexapi.swansong.ShaderTessMixin")),

    Triangulator(Phase.EARLY,
                 () -> ModuleConfig.TRIANGULATOR,
                 client("triangulator.RenderBlocksMixin",
                        "triangulator.RenderGlobalMixin",
                        "triangulator.RenderingRegistryMixin",
                        "triangulator.TessellatorMixin",
                        "triangulator.WorldRendererMixin")),
    Triangulator_NoFoamFix(Phase.EARLY,
                           () -> ModuleConfig.TRIANGULATOR && !ModuleConfig.BSP_SORTING,
                           mods(avoid(FoamFix)),
                           client("triangulator.TessellatorMixin_AvoidFoamFix")),
    Triangulator_FoamFix(Phase.EARLY,
                         () -> ModuleConfig.TRIANGULATOR && !ModuleConfig.BSP_SORTING,
                         mods(require(FoamFix)),
                         client("triangulator.TessellatorMixin_RequireFoamFix")),
    Triangulator_NoOptiFine(Phase.EARLY,
                            () -> ModuleConfig.TRIANGULATOR,
                            mods(avoid(OptiFine), avoid(SwanSong)),
                            client("triangulator.optifine.TessellatorVanillaMixin")),
    Triangulator_NoShader(Phase.EARLY,
                          () -> ModuleConfig.TRIANGULATOR,
                          mods(avoid(OptiFineShadersMod), avoid(SwanSong)),
                          client("triangulator.optifine.TessellatorVanillaOrOldOptifineMixin")),
    Triangulator_OptiFineShaders(Phase.EARLY,
                                 () -> ModuleConfig.TRIANGULATOR,
                                 require(OptiFineShadersMod),
                                 client("triangulator.optifine.TessellatorOptiFineMixin")),
    Triangulator_NoOptiFineHook(Phase.EARLY,
                                () -> ModuleConfig.TRIANGULATOR,
                                avoid(OptiFine),
                                client("triangulator.optifine.TessellatorVanillaHookMixin")),
    Triangulator_OptiFineHook(Phase.EARLY,
                              () -> ModuleConfig.TRIANGULATOR,
                              require(OptiFine),
                              client("triangulator.optifine.TessellatorOptiFineHookMixin")),
    Triangulator_RedstonePaste(Phase.LATE,
                               () -> ModuleConfig.TRIANGULATOR,
                               require(RedstonePaste),
                               client("triangulator.redstonepaste.RedstonePasteHighlighterMixin")),

    CrackFix(Phase.EARLY,
             () -> ModuleConfig.blockCrackFix,
             client("crackfix.RenderBlocksMixin")),

    AOFix_Universal(Phase.EARLY,
                    () -> ModuleConfig.aoFix && AOFixConfig.universalPatch,
                    client("ao.RenderBlocksUniversalMixin")),
    AOFix_Compat(Phase.EARLY,
                 () -> ModuleConfig.aoFix && AOFixConfig.renderHookCompatMode,
                 client("ao.RenderBlocksCompatMixin")),
    AOFix_Perf(Phase.EARLY,
               () -> ModuleConfig.aoFix && !AOFixConfig.renderHookCompatMode,
               client("ao.RenderBlocksPerformanceMixin")),
    AOFix_LittleTiles(Phase.LATE,
                      () -> ModuleConfig.aoFix && AOFixConfig.patchLittleTiles,
                      require(LittleTiles),
                      client("ao.littletiles.LittleBlockRenderHelperMixin")),

    ClippingHelper(Phase.EARLY,
                   () -> ModuleConfig.CLIPPING_HELPER_OPTS,
                   client("camera.Perf_ClippingHelperMixin")),
    ClippingHelper_NoFastCraft(Phase.EARLY,
                               () -> ModuleConfig.CLIPPING_HELPER_OPTS,
                               avoid(FastCraft),
                               client("camera.Perf_ClippingHelperImplMixin_NoFastCraft")),
    ClippingHelper_Multi(Phase.EARLY,
                         () -> ModuleConfig.CLIPPING_HELPER_ONCE_PER_FRAME,
                         client("camera.Multi_ClippingHelperImplMixin")),
    ClippingHelper_Multi_SwanSong(Phase.EARLY,
                                  ModuleConfig::THREADED_CHUNK_UPDATES,
                                  require(SwanSong),
                                  client("camera.swansong.Multi_ShaderEngineMixin")),

    RenderDistance(Phase.EARLY,
                   () -> ModuleConfig.UNLOCK_RENDER_DISTANCE,
                   client("camera.UnlockRD_GameSettingsMixin",
                          "camera.UnlockRD_GameSettingsOptionsMixin"),
                   common("camera.UnlockRD_PlayerManagerMixin")),

    ThreadedUpdates(Phase.EARLY,
                    ModuleConfig::THREADED_CHUNK_UPDATES,
                    client("threadedupdates.GameSettingsMixin",
                           "threadedupdates.OpenGLHelperMixin",
                           "threadedupdates.ChunkProviderClientMixin",
                           "threadedupdates.ForgeHooksClientMixin",
                           "threadedupdates.RenderingRegistryMixin",
                           "threadedupdates.TessellatorMixin",
                           "threadedupdates.ChunkCacheMixin",
                           "threadedupdates.blockbounds.BlockMixin_Root",
                           "threadedupdates.blockbounds.BlockMixin_Impl")),
    ThreadedUpdates_blockBounds(Phase.EARLY,
                                () -> ModuleConfig.THREADED_CHUNK_UPDATES() && ThreadingConfig.FAST_THREADED_BLOCK_BOUNDS,
                                client("threadedupdates.blockbounds.BlockMixin_FastImpl")),
    ThreadedUpdates_debug(Phase.EARLY,
                          () -> ModuleConfig.THREADED_CHUNK_UPDATES() && !ThreadingConfig.FAST_SAFETY_CHECKS,
                          client("threadedupdates.TessellatorMixin_Debug")),
    ThreadedUpdates_debugFast(Phase.EARLY,
                              () -> ModuleConfig.THREADED_CHUNK_UPDATES() && ThreadingConfig.FAST_SAFETY_CHECKS,
                              client("threadedupdates.TessellatorMixin_DebugFast")),
    ThreadedUpdates_NuclearControl(Phase.LATE,
                                   ModuleConfig::THREADED_CHUNK_UPDATES,
                                   require(NuclearControl),
                                   client("threadedupdates.nuclearcontrol.MainBlockRendererMixin",
                                          "threadedupdates.nuclearcontrol.TileEntityInfoPanelRendererMixin",
                                          "threadedupdates.nuclearcontrol.TileEntityAdvancedInfoPanelMixin")),
    ThreadedUpdates_OpenComputers(Phase.EARLY,
                                  ModuleConfig::THREADED_CHUNK_UPDATES,
                                  require(OpenComputers),
                                  client("threadedupdates.opencomputers.RenderStateMixin")),

    ThreadedUpdates_Computronics(Phase.LATE,
                                 ModuleConfig::THREADED_CHUNK_UPDATES,
                                 require(Computronics),
                                 client("threadedupdates.computronics.LampRenderMixin")),
    ThreadedUpdates_ExtraCells(Phase.LATE,
                               ModuleConfig::THREADED_CHUNK_UPDATES,
                               require(ExtraCells),
                               client("threadedupdates.extracells.RendererHardMEDriveMixin")),
    ThreadedUpdates_Automagy(Phase.LATE,
                             ModuleConfig::THREADED_CHUNK_UPDATES,
                             require(Automagy),
                             client("threadedupdates.automagy.RenderBlockGlowOverlayMixin")),
    ThreadedUpdates_Techguns(Phase.LATE,
                             ModuleConfig::THREADED_CHUNK_UPDATES,
                             require(Techguns),
                             client("threadedupdates.techguns.RenderLadderMixin")),
    ThreadedUpdates_Malisis(Phase.EARLY,
                            ModuleConfig::THREADED_CHUNK_UPDATES,
                            require(Malisis),
                            client("threadedupdates.malisis.MalisisRendererMixin",
                                   "threadedupdates.malisis.ParameterMixin")),
    ThreadedUpdates_Malisis_NH(Phase.EARLY,
                               ModuleConfig::THREADED_CHUNK_UPDATES,
                               require(Malisis_NH),
                               client("threadedupdates.malisis.nh.ParameterMixin")),
    ThreadedUpdates_NotFine(Phase.EARLY,
                            ModuleConfig::THREADED_CHUNK_UPDATES,
                            require(NotFine),
                            client("threadedupdates.notfine.CTMUtilsMixin")),
    ThreadedUpdates_StorageDrawers(Phase.LATE,
                                   ModuleConfig::THREADED_CHUNK_UPDATES,
                                   mods(require(StorageDrawers), avoid(StorageDrawers_ThreadSafe)),
                                   client("threadedupdates.storagedrawers.RenderHelperMixin",
                                          "threadedupdates.storagedrawers.CommonDrawerRendererMixin",
                                          "threadedupdates.storagedrawers.CommonFramingRendererMixin",
                                          "threadedupdates.storagedrawers.CommonTrimRendererMixin",
                                          "threadedupdates.storagedrawers.ControllerRendererMixin",
                                          "threadedupdates.storagedrawers.DrawersItemRendererMixin",
                                          "threadedupdates.storagedrawers.DrawersRendererMixin",
                                          "threadedupdates.storagedrawers.FramingTableRendererMixin",
                                          "threadedupdates.storagedrawers.ModularBoxRendererMixin",
                                          "threadedupdates.storagedrawers.PanelBoxRendererMixin")),
    ThreadedUpdates_ThermalExpansion(Phase.LATE,
                                     ModuleConfig::THREADED_CHUNK_UPDATES,
                                     require(ThermalExpansion),
                                     client("threadedupdates.thermalexpansion.BlockCacheMixin",
                                            "threadedupdates.thermalexpansion.BlockCellMixin",
                                            "threadedupdates.thermalexpansion.BlockDeviceMixin",
                                            "threadedupdates.thermalexpansion.BlockEnderMixin",
                                            "threadedupdates.thermalexpansion.BlockFrameMixin",
                                            "threadedupdates.thermalexpansion.BlockLightMixin",
                                            "threadedupdates.thermalexpansion.BlockMachineMixin",
                                            "threadedupdates.thermalexpansion.BlockSpongeMixin",
                                            "threadedupdates.thermalexpansion.BlockTankMixin",
                                            "threadedupdates.thermalexpansion.RenderCellMixin",
                                            "threadedupdates.thermalexpansion.RenderFrameMixin",
                                            "threadedupdates.thermalexpansion.RenderLightMixin",
                                            "threadedupdates.thermalexpansion.RenderTankMixin",
                                            "threadedupdates.thermalexpansion.RenderTesseractMixin")),

    AnimFix(Phase.EARLY,
            () -> ModuleConfig.TEXTURE_OPTIMIZATIONS,
            client("animfix.TextureMap_CommonMixin",
                   "animfix.StitcherMixin",
                   "animfix.StitcherSlotMixin")),
    AnimFix_Unprofiled(Phase.EARLY,
                       () -> ModuleConfig.TEXTURE_OPTIMIZATIONS && !ModuleConfig.ADVANCED_PROFILER,
                       client("animfix.TextureMap_UnprofiledMixin",
                              "animfix.TextureUtil_UnprofiledMixin")),
    AnimFix_Profiled(Phase.EARLY,
                     () -> ModuleConfig.TEXTURE_OPTIMIZATIONS && ModuleConfig.ADVANCED_PROFILER,
                     client("animfix.TextureMap_ProfiledMixin",
                            "animfix.TextureUtil_ProfiledMixin")),
    AnimFix_FastCraft(Phase.EARLY,
                      () -> ModuleConfig.TEXTURE_OPTIMIZATIONS,
                      require(FastCraft),
                      client("animfix.fastcraft.AbstractTextureMixin",
                             "animfix.fastcraft.DynamicTextureMixin",
                             "animfix.fastcraft.TextureMapMixin",
                             "animfix.fastcraft.TextureUtilMixin")),

    Voxelizer(Phase.EARLY,
              () -> ModuleConfig.VOXELIZER,
              client("voxelizer.ItemRendererMixin",
                     "voxelizer.RenderBlocksMixin",
                     "voxelizer.RenderItemMixin",
                     "voxelizer.TextureAtlasSpriteMixin",
                     "voxelizer.TextureManagerMixin",
                     "voxelizer.TextureMapMixin")),
    Voxelizer_OptiFine(Phase.EARLY,
                       () -> ModuleConfig.VOXELIZER,
                       require(OptiFine),
                       client("voxelizer.OFItemRendererMixin")),
    Voxelizer_RailCraft(Phase.LATE,
                        () -> ModuleConfig.VOXELIZER,
                        require(RailCraft),
                        client("voxelizer.railcraft.RenderTrackMixin")),

    MipMapFix(Phase.EARLY,
              () -> ModuleConfig.MIPMAP_FIX,
              client("mipmapfix.TextureMapMixin")),
    MipMapFix_NotOptiFine(Phase.EARLY,
                          () -> ModuleConfig.MIPMAP_FIX,
                          avoid(OptiFine),
                          client("mipmapfix.TextureUtilMixin")),

    Profiler(Phase.EARLY,
             () -> ModuleConfig.ADVANCED_PROFILER,
             client("profiler.MinecraftMixin",
                    "profiler.ProfilerMixin")),

    BSPSorting(Phase.EARLY,
               () -> ModuleConfig.BSP_SORTING,
               client("bsp.TessellatorMixin")),
    BSPSorting_NotFoamFix(Phase.EARLY,
                          () -> ModuleConfig.BSP_SORTING,
                          avoid(FoamFix),
                          client("bsp.TessellatorBSPSortingMixin")),
    BSPSorting_FoamFix(Phase.EARLY,
                       () -> ModuleConfig.BSP_SORTING,
                       require(FoamFix),
                       client("bsp.foamfix.TessellatorBSPSortingMixin")),

    DynamicLights_Standalone(Phase.EARLY,
                             () -> ModuleConfig.DYNAMIC_LIGHTS,
                             avoid(OptiFineDynamicLights),
                             client("dynlights.ItemRendererMixin",
                                    "dynlights.RenderGlobalMixin",
                                    "dynlights.WorldClientMixin")),
    DynamicLights_Standalone_NonThread(Phase.EARLY,
                                       () -> ModuleConfig.DYNAMIC_LIGHTS && !ModuleConfig.THREADED_CHUNK_UPDATES(),
                                       avoid(OptiFineDynamicLights),
                                       client("dynlights.nonthread.WorldClientMixin")),
    DynamicLights_Standalone_Thread(Phase.EARLY,
                                    () -> ModuleConfig.DYNAMIC_LIGHTS && ModuleConfig.THREADED_CHUNK_UPDATES(),
                                       avoid(OptiFineDynamicLights),
                                       client("dynlights.thread.WorldClientMixin")),
    DynamicLights_OptiFine(Phase.EARLY,
                           () -> ModuleConfig.DYNAMIC_LIGHTS,
                           require(OptiFineDynamicLights),
                           client("dynlights.of.DynamicLightsMixin")),

    ChunkCache(Phase.EARLY,
               () -> ModuleConfig.DYNAMIC_LIGHTS || ModuleConfig.FASTER_CHUNK_CACHE,
               avoid(OptiFineDynamicLights),
               client("cc.WorldRendererMixin")),
    ChunkCache_OptiFine_Shaders(Phase.EARLY,
                                require(OptiFineShadersMod),
                                client("cc.of.ChunkCacheOF_ShaderMixin")),
    ChunkCache_OptiFine_NonShaders(Phase.EARLY,
                                   mods(require(OptiFineDynamicLights), avoid(OptiFineShadersMod)),
                                   client("cc.of.ChunkCacheOF_NonShaderMixin")),

    ItemRenderList(Phase.EARLY,
                   () -> ModuleConfig.ITEM_RENDER_LISTS,
                   client("misc.ItemRenderList_ItemRendererMixin")),
    FastItemTexturing(Phase.EARLY,
                      () -> ModuleConfig.FAST_ITEM_ENTITY_TEXTURE_SWITCHING == ModuleConfig.ItemTexturing.Fast,
                      client("misc.FastItemTexturing_RenderGlobalMixin",
                             "misc.FastItemTexturing_RenderItemMixin")),
    FastItemTexturing_Faster(Phase.EARLY,
                             () -> ModuleConfig.FAST_ITEM_ENTITY_TEXTURE_SWITCHING == ModuleConfig.ItemTexturing.Faster,
                             client("misc.FastItemTexturing_RenderItem_FasterMixin")),
    FastItemPhysics(Phase.EARLY,
                    () -> ModuleConfig.FAST_ITEM_ENTITY_PHYSICS,
                    common("misc.FastItemPhysics_WorldMixin")),
    MinecartEarBlast(Phase.EARLY,
                     () -> ModuleConfig.MINECART_EAR_BLAST_FIX,
                     client("misc.MinecartEarBlast_WorldClientMixin")),
    BeaconFix(Phase.EARLY,
              () -> ModuleConfig.BEACON_OPTIMIZATION,
              client("misc.BeaconFix_TileEntityBeaconMixin",
                     "misc.BeaconFix_TileEntityBeaconRendererMixin")),
    TileEntitySorting(Phase.EARLY,
                      () -> ModuleConfig.TE_TRANSPARENCY_FIX,
                      client("misc.TileEntitySorting_RenderGlobalMixin")),
    TranslucentBlockLayers(Phase.EARLY,
                           () -> ModuleConfig.BLOCK_LAYER_TRANSPARENCY_FIX,
                           client("misc.TranslucentBlockLayers_RenderGlobalMixin")),
    SkyFix(Phase.EARLY,
           () -> ModuleConfig.SKY_MESH_OPTIMIZATION,
           client("misc.SkyFix_RenderGlobalMixin")),
    RealmShutUp(Phase.EARLY,
                () -> ModuleConfig.NO_REALMS_ON_MENU,
                client("misc.RealmShutUp_GuiMainMenuMixin",
                       "misc.RealmShutUp_RealmsBridgeMixin")),
    OverlayCrashFix(Phase.EARLY,
                    () -> ModuleConfig.OVERLAY_CRASH_FIX,
                    client("misc.OverlayCrashFix_ItemRendererMixin")),
    ParticleTransparency(Phase.EARLY,
                         () -> ModuleConfig.PARTICLE_TRANSPARENCY_FIX,
                         client("misc.ParticleTransparency_EffectRendererMixin")),
    PendingBlockUpdates(Phase.EARLY,
                        () -> ModuleConfig.PARTICLE_TRANSPARENCY_FIX,
                        avoid(CoreTweaks),
                        common("misc.PendingBlockUpdates_WorldServerMixin")),

    CubicParticles(Phase.EARLY,
                   () -> ModuleConfig.CUBIC_PARTICLES,
                   client("cubicparticles.EntityDiggingFXMixin")),

    DebugPatches(Phase.EARLY,
                 () -> Debug.ENABLED,
                 client("debug.TileEntityRendererDispatcherMixin",
                        "debug.WorldRendererMixin")),

    RenderingSafety(Phase.EARLY,
                    () -> ModuleConfig.RENDERING_SAFETY,
                    client("rendersafety.ForgeHooksClientMixin",
                           "rendersafety.ItemRendererMixin",
                           "rendersafety.RenderHelperMixin",
                           "rendersafety.RenderingRegistryMixin")),
    RenderingSafety_NoDragonAPI(Phase.EARLY,
                                () -> ModuleConfig.RENDERING_SAFETY,
                                avoid(DragonAPI),
                                client("rendersafety.TileEntityRendererDispatcherMixin")),
    RenderingSafety_DragonAPI(Phase.EARLY,
                              () -> ModuleConfig.RENDERING_SAFETY,
                              require(DragonAPI),
                              client("rendersafety.dragonapi.TileEntityRenderEventMixin")),

    OptiSpam(Phase.EARLY,
             () -> ModuleConfig.OPTIFINE_LOGSPAM_FIX,
             require(OptiFineShadersMod),
             client("optispam.BlockAliasesMixin",
                    "optispam.ConnectedParserMixin",
                    "optispam.ShaderExpressionResolverMixin",
                    "optispam.ShaderPackParserMixin")),

    Compat_SecurityCraft(Phase.LATE,
                         require(SecurityCraft),
                         common("compat.sc.BlockReinforcedFenceGateMixin",
                                "compat.sc.BlockReinforcedGlassPaneMixin",
                                "compat.sc.BlockReinforcedIronBarsMixin",
                                "compat.sc.BlockReinforcedStainedGlassPanesMixin")),
    // @formatter:on

    //region boilerplate
    ;
    @Getter
    private final MixinBuilder builder;

    Mixin(Phase phase, SidedMixins... mixins) {
        this(builder(mixins).setPhase(phase));
    }

    Mixin(Phase phase, BooleanSupplier cond, SidedMixins... mixins) {
        this(builder(cond, mixins).setPhase(phase));
    }

    Mixin(Phase phase, TaggedMod mod, SidedMixins... mixins) {
        this(builder(mod, mixins).setPhase(phase));
    }

    Mixin(Phase phase, TaggedMod[] mods, SidedMixins... mixins) {
        this(builder(mods, mixins).setPhase(phase));
    }

    Mixin(Phase phase, BooleanSupplier cond, TaggedMod mod, SidedMixins... mixins) {
        this(builder(cond, mod, mixins).setPhase(phase));
    }

    Mixin(Phase phase, BooleanSupplier cond, TaggedMod[] mods, SidedMixins... mixins) {
        this(builder(cond, mods, mixins).setPhase(phase));
    }

    private static SidedMixins common(@Language(value = "JAVA",
                                                prefix = "import " + Tags.ROOT_PKG + ".mixin.mixins.common.",
                                                suffix = ";") String... mixins) {
        return MixinHelper.common(mixins);
    }

    private static SidedMixins client(@Language(value = "JAVA",
                                                prefix = "import " + Tags.ROOT_PKG + ".mixin.mixins.client.",
                                                suffix = ";") String... mixins) {
        return MixinHelper.client(mixins);
    }

    private static SidedMixins server(@Language(value = "JAVA",
                                                prefix = "import " + Tags.ROOT_PKG + ".mixin.mixins.server.",
                                                suffix = ";") String... mixins) {
        return MixinHelper.server(mixins);
    }
    //endregion
}
