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

package com.falsepattern.falsetweaks.proxy;

import com.falsepattern.falsetweaks.Compat;
import com.falsepattern.falsetweaks.Share;
import com.falsepattern.falsetweaks.asm.FalseTweaksTransformer;
import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.falsetweaks.modules.leakfix.LeakFix;
import com.falsepattern.falsetweaks.modules.occlusion.OcclusionHelpers;
import com.falsepattern.falsetweaks.modules.renderlists.ItemRenderListManager;
import com.falsepattern.falsetweaks.modules.renderlists.VoxelRenderListManager;
import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadedChunkUpdateHelper;
import com.falsepattern.falsetweaks.modules.triangulator.calibration.Calibration;
import com.falsepattern.falsetweaks.modules.voxelizer.loading.LayerMetadataSection;
import com.falsepattern.falsetweaks.modules.voxelizer.loading.LayerMetadataSerializer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraftforge.client.ClientCommandHandler;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        if (!FMLClientHandler.instance().hasOptifine()) {
            //Load perf
            FalseTweaksTransformer.TRANSFORMERS.remove(FalseTweaksTransformer.OPTIFINE_DEOPTIMIZER);
        }
        if (LeakFix.ENABLED) {
            LeakFix.registerBus();
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
            OcclusionHelpers.init();
        }
    }

    @Override
    public void init(FMLInitializationEvent e) {
        if (ModuleConfig.OCCLUSION_TWEAKS && ModuleConfig.THREADED_CHUNK_UPDATES) {
            ThreadedChunkUpdateHelper.instance = new ThreadedChunkUpdateHelper();
            ThreadedChunkUpdateHelper.instance.init();
        }
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
        if (LeakFix.ENABLED) {
            LeakFix.gc();
        }

        if (ModuleConfig.TRIANGULATOR) {
            ClientCommandHandler.instance.registerCommand(new Calibration.CalibrationCommand());
        }
        Compat.applyCompatibilityTweaks();
    }
}
