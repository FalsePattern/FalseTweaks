package com.falsepattern.triangulator;

import com.falsepattern.lib.config.SimpleGuiFactory;
import net.minecraft.client.gui.GuiScreen;

public class TriangulatorGuiFactory implements SimpleGuiFactory {
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return TriangulatorGuiConfig.class;
    }
}
