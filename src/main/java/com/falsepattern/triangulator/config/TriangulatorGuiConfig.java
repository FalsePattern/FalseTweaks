package com.falsepattern.triangulator.config;

import com.falsepattern.lib.config.ConfigException;
import com.falsepattern.lib.config.SimpleGuiConfig;
import com.falsepattern.triangulator.Tags;

import net.minecraft.client.gui.GuiScreen;

public class TriangulatorGuiConfig extends SimpleGuiConfig {
    public TriangulatorGuiConfig(GuiScreen parent) throws ConfigException {
        super(parent, TriConfig.class, Tags.MODID, Tags.MODNAME);
    }
}
