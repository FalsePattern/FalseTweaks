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

package com.falsepattern.falsetweaks.mixin.mixins.client.misc;

import com.falsepattern.falsetweaks.Compat;
import lombok.val;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityBeaconRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.ResourceLocation;

@Mixin(TileEntityBeaconRenderer.class)
public abstract class BeaconFix_TileEntityBeaconRendererMixin extends TileEntitySpecialRenderer {
    @Shadow
    @Final
    private static ResourceLocation field_147523_b;

    private static int displayList;

    private static void drawQuad(Tessellator tess, double aX, double bX, double aZ, double bZ) {
        val yMax = 256d;
        val uMin = 0d;
        val uMax = 1d;
        val vMin = 0d;
        val vMax = 256d;
        tess.addVertexWithUV(aX, yMax, aZ, uMax, vMax);
        tess.addVertexWithUV(aX, 0, aZ, uMax, vMin);
        tess.addVertexWithUV(bX, 0, bZ, uMin, vMin);
        tess.addVertexWithUV(bX, yMax, bZ, uMin, vMax);
    }

    private static void drawBeam(Tessellator tess, int alpha) {
        val topRightX = 0.15d;
        val topRightZ = 0.15d;
        val topLeftX = -0.15d;
        val topLeftZ = 0.15d;
        val bottomRightX = 0.15d;
        val bottomRightZ = -0.15d;
        val bottomLeftX = -0.15d;
        val bottomLeftZ = -0.15d;
        tess.startDrawingQuads();
        tess.setColorRGBA(255, 255, 255, alpha);
        drawQuad(tess, topRightX, topLeftX, topRightZ, topLeftZ);
        drawQuad(tess, bottomLeftX, bottomRightX, bottomLeftZ, bottomRightZ);
        drawQuad(tess, topLeftX, bottomLeftX, topLeftZ, bottomLeftZ);
        drawQuad(tess, bottomRightX, topRightX, bottomRightZ, topRightZ);
        tess.draw();
        GL11.glPopMatrix();
    }

    private static void runDisplayList() {
        if (displayList == 0) {
            displayList = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(displayList, GL11.GL_COMPILE);
            Tessellator tess = Compat.tessellator();
            // @formatter:off
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT |
                              GL11.GL_TEXTURE_BIT |
                              GL11.GL_COLOR_BUFFER_BIT |
                              GL11.GL_DEPTH_BUFFER_BIT |
                              GL11.GL_TRANSFORM_BIT);
            // @formatter:on
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497.0F);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10497.0F);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_FOG);
            GL11.glDepthMask(true);
            OpenGlHelper.glBlendFunc(770, 1, 1, 0);
            drawBeam(tess, 32);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glDepthMask(false);
            GL11.glScaled(2, 1, 2);
            drawBeam(tess, 64);
            GL11.glMatrixMode(GL11.GL_TEXTURE);
            GL11.glPopMatrix();
            GL11.glPopAttrib();
            GL11.glEndList();
        }
        GL11.glCallList(displayList);
    }

    @Inject(method = "renderTileEntityAt(Lnet/minecraft/tileentity/TileEntityBeacon;DDDF)V",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private void renderTileEntityAt(TileEntityBeacon beacon, double x, double y, double z, float partialTickTime, CallbackInfo ci) {
        ci.cancel();
        float f1 = beacon.func_146002_i();

        if (f1 > 0.0F) {
            bindTexture(field_147523_b);
            val ptt = (double) partialTickTime;
            val integerTickTime = beacon.getWorldObj().getTotalWorldTime();
            val rotation = ((integerTickTime % 180) + ptt) * 2;
            val textureMove = ((integerTickTime % 10) + ptt) / 10;
            GL11.glMatrixMode(GL11.GL_TEXTURE);
            GL11.glPushMatrix();
            GL11.glTranslated(0, -textureMove, 0);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPushMatrix();
            GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
            GL11.glPushMatrix();
            GL11.glRotated(rotation, 0, 1, 0);
            runDisplayList();
        }
    }
}
