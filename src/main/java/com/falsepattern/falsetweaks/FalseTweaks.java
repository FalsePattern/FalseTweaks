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

package com.falsepattern.falsetweaks;

import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.falsetweaks.proxy.CommonProxy;
import lombok.val;
import lombok.var;

import net.minecraft.util.EnumChatFormatting;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.FMLLaunchHandler;

import javax.swing.JOptionPane;

@Mod(modid = Tags.MOD_ID,
     version = Tags.MOD_VERSION,
     name = Tags.MOD_NAME,
     acceptedMinecraftVersions = "[1.7.10]",
     guiFactory = Tags.ROOT_PKG + ".config.FalseTweaksGuiFactory",
     acceptableRemoteVersions = "*",
     dependencies = "required-after:falsepatternlib@[1.9.1,);" +
                    "after:gtnhlib@[0.5.21,);")
public class FalseTweaks {

    @SidedProxy(clientSide = Tags.ROOT_PKG + ".proxy.ClientProxy",
                serverSide = Tags.ROOT_PKG + ".proxy.ServerProxy")
    private static CommonProxy proxy;

    public FalseTweaks() {
    }

    private static void builtinMod(String modname) {
        createSidedException("Remove " + modname + " from your mods directory.\nIt has been merged into FalseTweaks!");
    }

    public static void createSidedException(String text) {
        var sanitizedText = text;
        for (val fmt: EnumChatFormatting.values()) {
            sanitizedText = sanitizedText.replace(fmt.toString(), "");
        }
        if (FMLLaunchHandler.side()
                            .isClient()) {
            if (Loader.isModLoaded("DragonRealmCore")) {
                JOptionPane.showMessageDialog(null,
                                              sanitizedText,
                                              "Failed to launch, the game will crash",
                                              JOptionPane.ERROR_MESSAGE);
            }
            throw ClientHelper.createException(text, sanitizedText);
        } else {
            throw new Error(sanitizedText);
        }
    }

    @Mod.EventHandler
    public void construct(FMLConstructionEvent e) {
        proxy.construct(e);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        if (Loader.isModLoaded("neodymium")) {
            createSidedException("FalseTweaks is not compatible with Neodymium since 4.0.0.\n" +
                                 "Replace Neodymium with Beddium.");
        }
        if (Loader.isModLoaded("optifine")) {
            if (ModuleConfig.THREADED_CHUNK_UPDATES()) {
                createSidedException("FalseTweaks threaded rendering is not compatible with OptiFine since 4.0.0.\n" +
                                     "Disable threaded rendering, or replace OptiFine with Beddium " +
                                     "(and SwanSong if you want shaders).");
            }
        }
        if (Loader.isModLoaded("animfix")) {
            builtinMod("animfix");
        }
        if (Loader.isModLoaded("triangulator")) {
            builtinMod("triangulator");
        }
        if (Loader.isModLoaded("DynamicLights")) {
            createSidedException(
                    "Remove the DynamicLights mod and restart the game!\n" +
                    "FalseTweaks has built-in dynamic lights support.");
        }
        proxy.preInit(e);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        proxy.init(e);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        proxy.postInit(e);
    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent e) {
        proxy.loadComplete(e);
    }

    private static class ClientHelper {
        private static RuntimeException createException(String text, String sanText) {
            return new MultiLineLoadingException(text, sanText);
        }
    }
}
