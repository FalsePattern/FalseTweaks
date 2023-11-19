/*
 * This file is part of FalseTweaks.
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

package com.falsepattern.falsetweaks.modules.triangulator.calibration;

import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.lib.compat.GuiLabel;
import com.falsepattern.lib.text.FormattedText;
import lombok.val;
import lombok.var;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class CalibrationGUI extends GuiScreen {
    private static final int ID_DESCRIPTION = 0;
    private static final int ID_TOGGLE = 1;
    private static final int ID_APPLY = 2;
    private final ResourceLocation reference = new ResourceLocation(Tags.MODID, "reference.png");
    protected List<GuiLabel> labelList = new ArrayList<>();
    private boolean flip = false;

    private int getYAfterComparison(int scale) {
        return 10 + scale * 4 + 20;
    }


    private void drawComparison(int scale) {
        val centerX = width / 2;
        var left = centerX - scale;
        var top = 10;
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LIGHTING_BIT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        drawQuad(left, top, scale, 3, 0xFFFF00, false, flip);
        drawQuad(left, top + scale, scale, 0, 0xFFFF00, false, !flip);
        drawQuad(left + scale, top + scale, scale, 1, 0xFFFF00, false, flip);
        drawQuad(left + scale, top, scale, 2, 0xFFFF00, false, !flip);
        top += scale * 2 + 10;
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        Minecraft.getMinecraft().getTextureManager().bindTexture(reference);
        drawQuad(left, top, scale * 2, 4, 0xFFFFFF, true, false);
        GL11.glPopAttrib();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initGui() {
        super.initGui();
        labelList.clear();
        val centerX = width / 2;
        var y = getYAfterComparison(25) + 5;
        val descriptionLabel = new GuiLabel(fontRendererObj, ID_DESCRIPTION, centerX, y, 0, 10, 0xFFFFFF);
        descriptionLabel.setCentered();
        for (var i = 0; ; i++) {
            val key = "gui.triangulator.calibrationdescription.line" + i;
            val translated = I18n.format(key);
            if (translated.equals("END") || translated.equals(key)) {
                break;
            }
            descriptionLabel.addLine(key);
            y += 10;
        }
        labelList.add(descriptionLabel);
        val toggle =
                new GuiButton(ID_TOGGLE, centerX - 110, y, 100, 20, I18n.format("gui.triangulator.calibration.toggle"));
        val apply =
                new GuiButton(ID_APPLY, centerX + 10, y, 100, 20, I18n.format("gui.triangulator.calibration.apply"));
        buttonList.add(toggle);
        buttonList.add(apply);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case ID_TOGGLE:
                flip = !flip;
                break;
            case ID_APPLY:
                Calibration.setCalibration(flip);
                val mc = Minecraft.getMinecraft();
                mc.displayGuiScreen(null);
                FormattedText.parse(EnumChatFormatting.GREEN + I18n.format("chat.triangulator.calibrated.message"))
                             .addChatMessage(mc.thePlayer);
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawComparison(25);
        for (GuiLabel guiLabel : this.labelList) {
            guiLabel.drawLabel(this.mc, mouseX, mouseY);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }


    private void drawQuad(float x, float y, float scale, int blackIndex, int rgb, boolean texture, boolean alt) {
        GL11.glBegin(GL11.GL_QUADS);
        int r = (rgb >>> 16) & 0xff;
        int g = (rgb >>> 8) & 0xff;
        int b = rgb & 0xff;
        int[] map = alt ? new int[]{1, 2, 3, 0} : new int[]{0, 1, 2, 3};
        for (int i = 0; i < 4; i++) {
            int j = map[i];
            int color = j == blackIndex ? 0 : 1;
            GL11.glColor3f(r * color, g * color, b * color);
            float X = ((j == 0) || (j == 3)) ? (x + scale) : x;
            float Y = ((j == 2) || (j == 3)) ? (y + scale) : y;
            if (texture) {
                GL11.glTexCoord2f((j == 0 || j == 3) ? 1 : 0, (j == 2 || j == 3) ? 1 : 0);
            }
            GL11.glVertex2f(X, Y);
        }
        GL11.glEnd();
    }
}
