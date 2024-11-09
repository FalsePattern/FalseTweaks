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

package com.falsepattern.falsetweaks;

import com.falsepattern.falsetweaks.proxy.CommonProxy;
import lombok.val;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;
import cpw.mods.fml.client.CustomModLoadingErrorDisplayException;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Tags.MOD_ID,
     version = Tags.MOD_VERSION,
     name = Tags.MOD_NAME,
     acceptedMinecraftVersions = "[1.7.10]",
     guiFactory = Tags.ROOT_PKG + ".config.FalseTweaksGuiFactory",
     acceptableRemoteVersions = "*",
     dependencies = "required-after:falsepatternlib@[1.4.2,);" +
                    "after:neodymium@[0.3.2,);"
     )
public class FalseTweaks {

    @SidedProxy(clientSide = Tags.ROOT_PKG + ".proxy.ClientProxy",
                serverSide = Tags.ROOT_PKG + ".proxy.ServerProxy")
    private static CommonProxy proxy;

    public FalseTweaks() {
    }

    @Mod.EventHandler
    public void construct(FMLConstructionEvent e) {
        proxy.construct(e);
    }

    private static CustomModLoadingErrorDisplayException builtinMod(String modname) {
        return new MultiLineLoadingException("Remove " + modname + " from your mods directory.\nIt has been merged into FalseTweaks!");
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        proxy.preInit(e);
        if (Loader.isModLoaded("animfix")) {
            throw builtinMod("animfix");
        }
        if (Loader.isModLoaded("triangulator")) {
            throw builtinMod("triangulator");
        }
        if (Loader.isModLoaded("DynamicLights")) {
            throw new MultiLineLoadingException("Remove the DynamicLights mod and restart the game!\nFalseTweaks has built-in dynamic lights support.");
        }
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

    private static class MultiLineLoadingException extends CustomModLoadingErrorDisplayException {
        private final String[] lines;
        public MultiLineLoadingException(String text) {
            lines = text.split("\n");
        }

        @Override
        public void initGui(GuiErrorScreen errorScreen, FontRenderer fontRenderer) {

        }

        @Override
        public void drawScreen(GuiErrorScreen errorScreen, FontRenderer fontRenderer, int mouseRelX, int mouseRelY, float tickTime) {
            int offset = errorScreen.height / 2 - (lines.length * 5);
            int x = errorScreen.width / 2;
            for (val line: lines) {
                errorScreen.drawCenteredString(fontRenderer, line, x, offset, 0xFFFFFF);
                offset += 10;
            }
        }
    }
}
