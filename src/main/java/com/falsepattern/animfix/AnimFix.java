package com.falsepattern.animfix;

import com.falsepattern.animfix.proxy.CommonProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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