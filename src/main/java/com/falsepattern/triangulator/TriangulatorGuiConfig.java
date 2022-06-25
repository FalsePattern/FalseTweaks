package com.falsepattern.triangulator;

import com.falsepattern.lib.config.ConfigException;
import com.falsepattern.lib.config.SimpleGuiConfig;
import net.minecraft.client.gui.GuiScreen;

public class TriangulatorGuiConfig extends SimpleGuiConfig {
    public TriangulatorGuiConfig(GuiScreen parent) throws ConfigException {
        super(parent, TriConfig.class, Tags.MODID, Tags.MODNAME);
    }
}
