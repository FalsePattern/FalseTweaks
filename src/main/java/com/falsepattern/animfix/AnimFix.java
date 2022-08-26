/*
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

package com.falsepattern.animfix;

import com.falsepattern.animfix.proxy.CommonProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Tags.MODID,
     version = Tags.VERSION,
     name = Tags.MODNAME,
     guiFactory = Tags.GROUPNAME + ".config.AnimFixGuiFactory",
     acceptedMinecraftVersions = "[1.7.10]",
     dependencies = "required-after:falsepatternlib@[0.9.1,);")
public class AnimFix {
    public static final Logger LOG = LogManager.getLogger(Tags.MODID);

    @SidedProxy(clientSide = Tags.GROUPNAME + ".proxy.ClientProxy",
                serverSide = Tags.GROUPNAME + ".proxy.ServerProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        proxy.preInit(e);
    }
}