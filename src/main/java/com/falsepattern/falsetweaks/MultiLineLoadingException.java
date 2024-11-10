package com.falsepattern.falsetweaks;

import lombok.val;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;
import cpw.mods.fml.client.CustomModLoadingErrorDisplayException;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
class MultiLineLoadingException extends CustomModLoadingErrorDisplayException {
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
        for (val line : lines) {
            errorScreen.drawCenteredString(fontRenderer, line, x, offset, 0xFFFFFF);
            offset += 10;
        }
    }
}
