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

package com.falsepattern.falsetweaks.mixin.plugin;

import com.falsepattern.falsetweaks.config.FTConfig;
import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.lib.mixin.IMixin;
import com.falsepattern.lib.mixin.ITargetedMod;
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
    //region Always Loaded
    //region Minecraft->client
    ItemRendererMixin(Side.CLIENT, always(), "vanilla.ItemRendererMixin"),
    QuadComparatorMixin(Side.CLIENT, always(), "vanilla.QuadComparatorMixin"),
    TileEntityBeaconRendererMixin(Side.CLIENT, always(), "vanilla.TileEntityBeaconRendererMixin"),
    TileEntityBeaconMixin(Side.CLIENT, always(), "vanilla.TileEntityBeaconMixin"),
    RenderBlocksMixin(Side.CLIENT, always(), "vanilla.RenderBlocksUltraMixin"),
    RenderBlocksCompatMixin(Side.CLIENT, condition(() -> FTConfig.RENDER_HOOK_COMPAT_MODE), "vanilla.RenderBlocksCompatMixin"),
    RenderBlocksPerformanceMixin(Side.CLIENT, condition(() -> !FTConfig.RENDER_HOOK_COMPAT_MODE), "vanilla.RenderBlocksPerformanceMixin"),
    RenderGlobalMixin(Side.CLIENT, always(), "vanilla.RenderGlobalMixin"),
    TessellatorMixin(Side.CLIENT, always(), "vanilla.TessellatorMixin"),
    TextureAtlasSpriteMixin(Side.CLIENT, always(), "vanilla.TextureAtlasSpriteMixin"),

    //leak fix
    LeakFixRenderGlobalMixin(Side.CLIENT, always(), "vanilla.leakfix.RenderGlobalMixin"),
    LeakFixWorldRendererMixin(Side.CLIENT, always(), "vanilla.leakfix.WorldRendererMixin"),
    //endregion Minecraft->client
    //region FoamFix->client
    FFTessellatorVanillaMixin(Side.CLIENT, avoid(TargetedMod.FOAMFIX), "foamfix.TessellatorVanillaMixin"),
    FFTessellatorFoamFixMixin(Side.CLIENT, require(TargetedMod.FOAMFIX), "foamfix.TessellatorFoamFixMixin"),
    //endregion FoamFix->client
    //region OptiFine->client
    OFTessellatorVanillaMixin(Side.CLIENT,
                              avoid(TargetedMod.OPTIFINE_WITHOUT_SHADERS).and(avoid(TargetedMod.OPTIFINE_WITH_SHADERS)),
                              "optifine.TessellatorVanillaMixin"),
    OFTessellatorVanillaOrOldOptifineMixin(Side.CLIENT, avoid(TargetedMod.OPTIFINE_WITH_SHADERS),
                                           "optifine.TessellatorVanillaOrOldOptifineMixin"),
    OFTessellatorOptiFineMixin(Side.CLIENT, require(TargetedMod.OPTIFINE_WITH_SHADERS),
                               "optifine.TessellatorOptiFineMixin"),

    //leak fix
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
    //endregion OptiFine->client
    //region FastCraft->client
    //leak fix
    FCGLAllocatorMixin(Side.CLIENT, require(TargetedMod.FASTCRAFT), "fastcraft.leakfix.GLAllocationMixin"),
    //endregion FastCraft->client
    //region ChromatiCraft->client
    CCRuneRendererMixin(Side.CLIENT, require(TargetedMod.CHROMATICRAFT), "chromaticraft.RuneRendererMixin"),
    //endregion ChromatiCraft->client
    //region RedstonePaste->client
    RedstonePasteHighlighterMixin(Side.CLIENT, require(TargetedMod.REDSTONEPASTE),
                                  "redstonepaste.RedstonePasteHighlighterMixin"),
    //endregion RedstonePaste->client

    //endregion Always Loaded

    //region Texture Optimizations Module
    //region Minecraft->client
    TextureMapMixin(Side.CLIENT, condition(() -> ModuleConfig.TEXTURE_OPTIMIZATIONS), "animfix.minecraft.TextureMapMixin"),
    TextureUtilMixin(Side.CLIENT, condition(() -> ModuleConfig.TEXTURE_OPTIMIZATIONS), "animfix.minecraft.TextureUtilMixin"),
    StitcherMixin(Side.CLIENT, condition(() -> ModuleConfig.TEXTURE_OPTIMIZATIONS), "animfix.minecraft.StitcherMixin"),
    StitcherSlotMixin(Side.CLIENT, condition(() -> ModuleConfig.TEXTURE_OPTIMIZATIONS), "animfix.minecraft.StitcherSlotMixin"),
    //endregion Minecraft->client
    //region FastCraft->client
    FCAbstractTextureMixin(Side.CLIENT, condition(() -> ModuleConfig.TEXTURE_OPTIMIZATIONS).and(require(TargetedMod.FASTCRAFT)), "animfix.fastcraft.AbstractTextureMixin"),
    FCDynamicTextureMixin(Side.CLIENT, condition(() -> ModuleConfig.TEXTURE_OPTIMIZATIONS).and(require(TargetedMod.FASTCRAFT)), "animfix.fastcraft.DynamicTextureMixin"),
    FCTextureMapMixin(Side.CLIENT, condition(() -> ModuleConfig.TEXTURE_OPTIMIZATIONS).and(require(TargetedMod.FASTCRAFT)), "animfix.fastcraft.TextureMapMixin"),
    FCTextureUtilMixin(Side.CLIENT, condition(() -> ModuleConfig.TEXTURE_OPTIMIZATIONS).and(require(TargetedMod.FASTCRAFT)), "animfix.fastcraft.TextureUtilMixin"),
    //endregion FastCraft->client
    //endregion Texture Optimizations Module

    //region Startup Optimizations Module
    //region Minecraft->client
    DirectoryDiscovererMixin(Side.COMMON, condition(() -> ModuleConfig.STARTUP_OPTIMIZATIONS), "regex.DirectoryDiscovererMixin"),
    JarDiscovererMixin(Side.COMMON, condition(() -> ModuleConfig.STARTUP_OPTIMIZATIONS), "regex.JarDiscovererMixin"),
    ModContainerFactoryMixin(Side.COMMON, condition(() -> ModuleConfig.STARTUP_OPTIMIZATIONS), "regex.ModContainerFactoryMixin"),
    ModDiscovererMixin(Side.COMMON, condition(() -> ModuleConfig.STARTUP_OPTIMIZATIONS), "regex.ModDiscovererMixin"),
    //endregion Minecraft->client
    //endregion Startup Optimizations Module

    //region Voxelizer Module
    VoxItemRendererMixin(Side.CLIENT, condition(() -> ModuleConfig.ITEM_VOXELIZER), "vanilla.itemvox.ItemRendererMixin"),
    VoxRenderItemMixin(Side.CLIENT, condition(() -> ModuleConfig.ITEM_VOXELIZER), "vanilla.itemvox.RenderItemMixin"),

    //endregion Voxelizer Module
    ;

    @Getter
    private final Side side;
    @Getter
    private final Predicate<List<ITargetedMod>> filter;
    @Getter
    private final String mixin;
}

