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

package com.falsepattern.falsetweaks.proxy;

import com.falsepattern.falsetweaks.Compat;
import com.falsepattern.falsetweaks.Share;
import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.falsetweaks.config.RenderDistanceConfig;
import com.falsepattern.falsetweaks.modules.beddium.BeddiumBridge;
import com.falsepattern.falsetweaks.modules.debug.Debug;
import com.falsepattern.falsetweaks.modules.dynlights.DynamicLightsDrivers;
import com.falsepattern.falsetweaks.modules.renderlists.ItemRenderListManager;
import com.falsepattern.falsetweaks.modules.renderlists.VoxelRenderListManager;
import com.falsepattern.falsetweaks.modules.threadedupdates.saftey.ThreadSafeBlockRendererMap;
import com.falsepattern.falsetweaks.modules.triangulator.calibration.Calibration;
import com.falsepattern.falsetweaks.modules.voxelizer.loading.LayerMetadataSection;
import com.falsepattern.falsetweaks.modules.voxelizer.loading.LayerMetadataSerializer;
import lombok.SneakyThrows;
import lombok.val;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ICrashCallable;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

import static com.falsepattern.falsetweaks.FalseTweaks.createSidedException;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {
    public static boolean clippingHelperShouldInit = true;

    @SneakyThrows
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        if (Loader.isModLoaded("angelica") && ModuleConfig.THREADED_CHUNK_UPDATES()) {
            createSidedException(
                    "FalseTweaks threaded rendering is not compatible with Angelica.\nPlease disable it in the FalseTweaks config.");
        }
        if (ModuleConfig.TEXTURE_OPTIMIZATIONS && Compat.isSTBIStitcher()) {
            createSidedException("FalseTweaks " +
                                 EnumChatFormatting.BOLD +
                                 "textureOptimizations" +
                                 EnumChatFormatting.RESET +
                                 " is not compatible with LWJGL3Ify's " +
                                 EnumChatFormatting.BOLD +
                                 "stbiTextureStitching" +
                                 EnumChatFormatting.RESET +
                                 " option.\nDisable stbiTextureStitching in the lwjgl3ify.cfg\nor disable textureOptimizations in FalseTweaks!");
        }
        FMLCommonHandler.instance()
                        .registerCrashCallable(new ICrashCallable() {
                            @Override
                            public String getLabel() {
                                return "FalseTweaks";
                            }

                            @Override
                            public String call() {
                                val sb = new StringBuilder();
                                if (ModuleConfig.THREADED_CHUNK_UPDATES()) {
                                    sb.append("Threaded chunk updates enabled");
                                }
                                return sb.toString();
                            }
                        });
        Share.LEAKFIX_CLASS_INITIALIZED = true;
        if (ModuleConfig.TRIANGULATOR) {
            Calibration.registerBus();
        }
        if (ModuleConfig.VOXELIZER) {
            Minecraft.getMinecraft().metadataSerializer_.registerMetadataSectionType(new LayerMetadataSerializer(),
                                                                                     LayerMetadataSection.class);
        }
        if (ModuleConfig.THREADED_CHUNK_UPDATES()) {
            ThreadSafeBlockRendererMap.inject();
        }
        if (Compat.beddiumInstalled()) {
            if (ModuleConfig.THREADED_CHUNK_UPDATES()) {
                BeddiumBridge.registerThreadingProvider();
            } else {
                BeddiumBridge.registerBasicProvider();
            }
        }
        FMLCommonHandler.instance()
                        .bus()
                        .register(this);
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        if (ModuleConfig.THREADED_CHUNK_UPDATES()) {
            if (Loader.isModLoaded("gtnhlib")) {
                //TODO nhlib compat
            }
        }
    }


    @SubscribeEvent
    public void onTick(TickEvent.RenderTickEvent e) {
        clippingHelperShouldInit = true;
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
        if (ModuleConfig.ITEM_RENDER_LISTS) {
            ((IReloadableResourceManager) Minecraft.getMinecraft()
                                                   .getResourceManager()).registerReloadListener(ItemRenderListManager.INSTANCE);
            if (ModuleConfig.VOXELIZER) {
                ((IReloadableResourceManager) Minecraft.getMinecraft()
                                                       .getResourceManager()).registerReloadListener(
                        VoxelRenderListManager.INSTANCE);
            }
        }

        if (ModuleConfig.TRIANGULATOR) {
            ClientCommandHandler.instance.registerCommand(new Calibration.CalibrationCommand());
        }

        if (Compat.dynamicLightsPresent()) {
            DynamicLightsDrivers.postInit();
        }
        Compat.applyCompatibilityTweaks();

        if (ModuleConfig.UNLOCK_RENDER_DISTANCE) {
            if (GameSettings.Options.RENDER_DISTANCE.getValueMax() != RenderDistanceConfig.RENDER_DISTANCE) {
                GameSettings.Options.RENDER_DISTANCE.setValueMax(RenderDistanceConfig.RENDER_DISTANCE);
            }
        }
        val settings = Minecraft.getMinecraft().gameSettings;
        settings.renderDistanceChunks = Math.min(settings.renderDistanceChunks,
                                                 (int) GameSettings.Options.RENDER_DISTANCE.getValueMax());
        if (Debug.ENABLED) {
            Debug.init();
        }
    }

    @Override
    public void loadComplete(FMLLoadCompleteEvent e) {
        super.loadComplete(e);
        ThreadSafeBlockRendererMap.logBrokenISBRHs();
        ThreadSafeBlockRendererMap.markThreadsafeBlocks();
    }

    @SubscribeEvent
    public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent e) {
        if (!e.modID.equals(Tags.MOD_ID)) {
            return;
        }

        // Ingame editable render distance limit
        if (ModuleConfig.UNLOCK_RENDER_DISTANCE) {
            GameSettings.Options.RENDER_DISTANCE.setValueMax(RenderDistanceConfig.RENDER_DISTANCE);
        }
    }
}
