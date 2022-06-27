package com.falsepattern.triangulator.calibration;

import com.falsepattern.lib.compat.GuiLabel;
import com.falsepattern.triangulator.Tags;
import com.falsepattern.triangulator.api.ToggleableTessellator;
import com.falsepattern.triangulator.mixin.helper.ITessellatorMixin;
import lombok.val;
import lombok.var;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class CalibrationGUI extends GuiScreen {
    private static final int ID_DESCRIPTION = 0;
    private static final int ID_TOGGLE = 1;
    private static final int ID_APPLY = 2;

    private boolean flip = false;
    private ResourceLocation reference = new ResourceLocation(Tags.MODID, "reference.png");

    protected List<GuiLabel> labelList = new ArrayList<>();

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
        val tess = Tessellator.instance;
        ((ToggleableTessellator)tess).disableTriangulator();
        tess.startDrawingQuads();
        ((ITessellatorMixin)tess).alternativeTriangulation(flip);
        drawQuad(left, top, scale, 3);
        ((ITessellatorMixin)tess).alternativeTriangulation(!flip);
        drawQuad(left, top + scale, scale, 0);
        ((ITessellatorMixin)tess).alternativeTriangulation(flip);
        drawQuad(left + scale, top + scale, scale, 1);
        ((ITessellatorMixin)tess).alternativeTriangulation(!flip);
        drawQuad(left + scale, top, scale, 2);
        top += scale * 2 + 10;
        tess.draw();
        ((ToggleableTessellator)tess).enableTriangulator();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        Minecraft.getMinecraft().getTextureManager().bindTexture(reference);
        tess.startDrawingQuads();
        tess.setColorOpaque_F(1, 1, 1);
        tess.addVertexWithUV(left + scale * 2, top, 0, 1, 0);
        tess.addVertexWithUV(left, top, 0, 0, 0);
        tess.addVertexWithUV(left, top + scale * 2, 0, 0, 1);
        tess.addVertexWithUV(left + scale * 2, top + scale * 2, 0, 1, 1);
        tess.draw();
        GL11.glPopAttrib();
    }

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
        val toggle = new GuiButton(ID_TOGGLE, centerX - 110, y, 100, 20, I18n.format("gui.triangulator.calibration.toggle"));
        val apply = new GuiButton(ID_APPLY, centerX + 10, y, 100, 20, I18n.format("gui.triangulator.calibration.apply"));
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
                Minecraft.getMinecraft().displayGuiScreen(null);
                break;
        }
    }

    @Override
    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_) {
        drawDefaultBackground();
        drawComparison(25);
        for (GuiLabel guiLabel : this.labelList) {
            guiLabel.drawLabel(this.mc, p_73863_1_, p_73863_2_);
        }
        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
    }


    private void drawQuad(float x, float y, float scale, int blackIndex) {
        val tess = Tessellator.instance;
        for (int j = 0; j < 4; j++) {
            float color = j == blackIndex ? 0 : 1;
            tess.setColorOpaque_F(color, color, 0);
            float X = ((j == 0) || (j == 3)) ? (x + scale) : x;
            float Y = ((j == 2) || (j == 3)) ? (y + scale) : y;
            tess.addVertex(X, Y, 0);
        }
    }

    private void drawQuadGLTris(float x, float y, float scale, float a, float b, float c, float d, boolean alternative) {
        GL11.glBegin(GL11.GL_TRIANGLES);
        if (alternative) {
            GL11.glColor3f(a, a, a);
            GL11.glVertex2f(x + scale, y);
            GL11.glColor3f(b, b, b);
            GL11.glVertex2f(x, y);
            GL11.glColor3f(d, d, d);
            GL11.glVertex2f(x + scale, y + scale);
            GL11.glColor3f(d, d, d);
            GL11.glVertex2f(x + scale, y + scale);
            GL11.glColor3f(b, b, b);
            GL11.glVertex2f(x, y);
            GL11.glColor3f(c, c, c);
            GL11.glVertex2f(x, y + scale);
        } else {
            GL11.glColor3f(a, a, a);
            GL11.glVertex2f(x + scale, y);
            GL11.glColor3f(b, b, b);
            GL11.glVertex2f(x, y);
            GL11.glColor3f(c, c, c);
            GL11.glVertex2f(x, y + scale);
            GL11.glColor3f(d, d, d);
            GL11.glVertex2f(x + scale, y + scale);
            GL11.glColor3f(a, a, a);
            GL11.glVertex2f(x + scale, y);
            GL11.glColor3f(c, c, c);
            GL11.glVertex2f(x, y + scale);
        }
        GL11.glEnd();
    }
}
