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

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Tags.MODID,
     version = Tags.VERSION,
     name = Tags.MODNAME,
     acceptedMinecraftVersions = "[1.7.10]",
     guiFactory = Tags.GROUPNAME + ".config.FalseTweaksGuiFactory",
     acceptableRemoteVersions = "*",
     dependencies = "required-after:falsepatternlib@[1.2.0,);" +
                    "after:neodymium@[0.3.2,);")
public class FalseTweaks {

    @SidedProxy(clientSide = Tags.GROUPNAME + ".proxy.ClientProxy",
                serverSide = Tags.GROUPNAME + ".proxy.ServerProxy")
    private static CommonProxy proxy;

    public FalseTweaks() {
    }

    @Mod.EventHandler
    public void construct(FMLConstructionEvent e) {
        proxy.construct(e);
    }

    private static Error idiot(String modname) {
        val loudWarning = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            loudWarning.append("REMOVE ").append(modname).append(" FROM YOUR MODS DIRECTORY, IT HAS BEEN MERGED INTO FALSETWEAKS\n");
        }
        return new Error(loudWarning.toString());
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        proxy.preInit(e);
        if (Loader.isModLoaded("animfix")) {
            throw idiot("animfix");
        }
        if (Loader.isModLoaded("triangulator")) {
            throw idiot("triangulator");
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
}
