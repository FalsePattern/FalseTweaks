package com.falsepattern.animfix.config;

import com.falsepattern.lib.config.SimpleGuiFactory;

import net.minecraft.client.gui.GuiScreen;

@SuppressWarnings("unused")
public class AnimFixGuiFactory implements SimpleGuiFactory {
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return AnimFixGuiConfig.class;
    }
}
