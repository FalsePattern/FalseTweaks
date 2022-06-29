package com.falsepattern.animfix.config;

import com.falsepattern.animfix.Tags;
import com.falsepattern.lib.config.ConfigException;
import com.falsepattern.lib.config.SimpleGuiConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class AnimFixGuiConfig extends SimpleGuiConfig {

    public AnimFixGuiConfig(GuiScreen parent) throws ConfigException {
        super(parent, AnimConfig.class, Tags.MODID, Tags.MODNAME);
    }

    @Override
    public void onGuiClosed() {
        Minecraft.getMinecraft().scheduleResourcesRefresh();
    }
}
