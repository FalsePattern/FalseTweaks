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

package com.falsepattern.falsetweaks.proxy;

import com.falsepattern.falsetweaks.Compat;
import com.falsepattern.falsetweaks.Share;
import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.falsetweaks.asm.FalseTweaksTransformer;
import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.falsetweaks.config.OcclusionConfig;
import com.falsepattern.falsetweaks.modules.occlusion.leakfix.LeakFix;
import com.falsepattern.falsetweaks.modules.occlusion.OcclusionHelpers;
import com.falsepattern.falsetweaks.modules.renderlists.ItemRenderListManager;
import com.falsepattern.falsetweaks.modules.renderlists.VoxelRenderListManager;
import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadedChunkUpdateHelper;
import com.falsepattern.falsetweaks.modules.triangulator.calibration.Calibration;
import com.falsepattern.falsetweaks.modules.voxelizer.loading.LayerMetadataSection;
import com.falsepattern.falsetweaks.modules.voxelizer.loading.LayerMetadataSerializer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.client.ClientCommandHandler;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {
    public static boolean clippingHelperShouldInit = true;
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        if (!FMLClientHandler.instance().hasOptifine()) {
            //Load perf
            FalseTweaksTransformer.TRANSFORMERS.remove(FalseTweaksTransformer.OPTIFINE_DEOPTIMIZER);
        }
        Share.LEAKFIX_CLASS_INITIALIZED = true;
        if (ModuleConfig.TRIANGULATOR) {
            Calibration.registerBus();
        }
        if (ModuleConfig.VOXELIZER) {
            Minecraft.getMinecraft().metadataSerializer_.registerMetadataSectionType(new LayerMetadataSerializer(),
                                                                                     LayerMetadataSection.class);
        }
        if (ModuleConfig.OCCLUSION_TWEAKS) {
            LeakFix.registerBus();
            OcclusionHelpers.init();
        }
        FMLCommonHandler.instance().bus().register(this);
    }

    @Override
    public void init(FMLInitializationEvent e) {
        if (ModuleConfig.OCCLUSION_TWEAKS && ModuleConfig.THREADED_CHUNK_UPDATES) {
            ThreadedChunkUpdateHelper.instance = new ThreadedChunkUpdateHelper();
            ThreadedChunkUpdateHelper.instance.init();
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
            ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(
                    ItemRenderListManager.INSTANCE);
            if (ModuleConfig.VOXELIZER) {
                ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(
                        VoxelRenderListManager.INSTANCE);
            }
        }
        LeakFix.gc();

        if (ModuleConfig.TRIANGULATOR) {
            ClientCommandHandler.instance.registerCommand(new Calibration.CalibrationCommand());
        }
        Compat.applyCompatibilityTweaks();

        // FastCraft compat
        if (ModuleConfig.OCCLUSION_TWEAKS && GameSettings.Options.RENDER_DISTANCE.getValueMax() != OcclusionConfig.RENDER_DISTANCE) {
            GameSettings.Options.RENDER_DISTANCE.setValueMax(OcclusionConfig.RENDER_DISTANCE);
        }
    }

    @SubscribeEvent
    public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent e) {
        if (!e.modID.equals(Tags.MODID))
            return;

        // Ingame editable occlusion tweaks
        if (ModuleConfig.OCCLUSION_TWEAKS) {
            GameSettings.Options.RENDER_DISTANCE.setValueMax(OcclusionConfig.RENDER_DISTANCE);
        }
    }
}
