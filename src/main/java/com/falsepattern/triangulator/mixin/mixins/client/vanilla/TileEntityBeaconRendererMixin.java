package com.falsepattern.triangulator.mixin.mixins.client.vanilla;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityBeaconRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityBeaconRenderer.class)
public abstract class TileEntityBeaconRendererMixin extends TileEntitySpecialRenderer {
    @Shadow @Final private static ResourceLocation field_147523_b;
    
    private static int displayList;
    
    private static void runDisplayList() {
        if (displayList == 0) {
            displayList = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(displayList, GL11.GL_COMPILE);
            Tessellator tessellator = Tessellator.instance;

            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497.0F);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10497.0F);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDepthMask(true);
            OpenGlHelper.glBlendFunc(770, 1, 1, 0);
            tessellator.startDrawingQuads();
            tessellator.setColorRGBA(255, 255, 255, 32);
            double topRightX = 0.15;
            double topRightZ = 0.15;
            double topLeftX = -0.15;
            double topLeftZ = 0.15;
            double bottomRightX = 0.15;
            double bottomRightZ = -0.15;
            double bottomLeftX = -0.15;
            double bottomLeftZ = -0.15;
            double yMax = 256;
            double uMin = 0;
            double uMax = 1;
            double vMin = 0;
            double vMax = 256;
            tessellator.addVertexWithUV(topRightX, 0, topRightZ, uMax, vMin);
            tessellator.addVertexWithUV(topRightX, yMax, topRightZ, uMax, vMax);
            tessellator.addVertexWithUV(topLeftX, 0, topLeftZ, uMin, vMin);
            tessellator.addVertexWithUV(topLeftX, yMax, topLeftZ, uMin, vMax);
            tessellator.addVertexWithUV(bottomLeftX, yMax, bottomLeftZ, uMax, vMax);
            tessellator.addVertexWithUV(bottomLeftX, 0, bottomLeftZ, uMax, vMin);
            tessellator.addVertexWithUV(bottomRightX, 0, bottomRightZ, uMin, vMin);
            tessellator.addVertexWithUV(bottomRightX, yMax, bottomRightZ, uMin, vMax);
            tessellator.addVertexWithUV(topLeftX, yMax, topLeftZ, uMax, vMax);
            tessellator.addVertexWithUV(topLeftX, 0, topLeftZ, uMax, vMin);
            tessellator.addVertexWithUV(bottomLeftX, 0, bottomLeftZ, uMin, vMin);
            tessellator.addVertexWithUV(bottomLeftX, yMax, bottomLeftZ, uMin, vMax);
            tessellator.addVertexWithUV(bottomRightX, yMax, bottomRightZ, uMax, vMax);
            tessellator.addVertexWithUV(bottomRightX, 0, bottomRightZ, uMax, vMin);
            tessellator.addVertexWithUV(topRightX, 0, topRightZ, uMin, vMin);
            tessellator.addVertexWithUV(topRightX, yMax, topRightZ, uMin, vMax);
            tessellator.draw();

            GL11.glPopMatrix();
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glDepthMask(false);
            tessellator.startDrawingQuads();
            tessellator.setColorRGBA(255, 255, 255, 64);
            topLeftX *= 2;
            topLeftZ *= 2;
            topRightX *= 2;
            topRightZ *= 2;
            bottomLeftX *= 2;
            bottomLeftZ *= 2;
            bottomRightX *= 2;
            bottomRightZ *= 2;
            tessellator.addVertexWithUV(topRightX, yMax, topRightZ, uMax, vMax);
            tessellator.addVertexWithUV(topRightX, 0, topRightZ, uMax, vMin);
            tessellator.addVertexWithUV(topLeftX, 0, topLeftZ, uMin, vMin);
            tessellator.addVertexWithUV(topLeftX, yMax, topLeftZ, uMin, vMax);
            tessellator.addVertexWithUV(bottomLeftX, yMax, bottomLeftZ, uMax, vMax);
            tessellator.addVertexWithUV(bottomLeftX, 0, bottomLeftZ, uMax, vMin);
            tessellator.addVertexWithUV(bottomRightX, 0, bottomRightZ, uMin, vMin);
            tessellator.addVertexWithUV(bottomRightX, yMax, bottomRightZ, uMin, vMax);
            tessellator.addVertexWithUV(topLeftX, yMax, topLeftZ, uMax, vMax);
            tessellator.addVertexWithUV(topLeftX, 0, topLeftZ, uMax, vMin);
            tessellator.addVertexWithUV(bottomLeftX, 0, bottomLeftZ, uMin, vMin);
            tessellator.addVertexWithUV(bottomLeftX, yMax, bottomLeftZ, uMin, vMax);
            tessellator.addVertexWithUV(bottomRightX, yMax, bottomRightZ, uMax, vMax);
            tessellator.addVertexWithUV(bottomRightX, 0, bottomRightZ, uMax, vMin);
            tessellator.addVertexWithUV(topRightX, 0, topRightZ, uMin, vMin);
            tessellator.addVertexWithUV(topRightX, yMax, topRightZ, uMin, vMax);
            tessellator.draw();
            GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_TEXTURE);
            GL11.glPopMatrix();
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDepthMask(true);
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

        if (f1 > 0.0F)
        {
            bindTexture(field_147523_b);
            double tickTime = (double)beacon.getWorldObj().getTotalWorldTime() + partialTickTime;
            double rotation = tickTime * 2;
            double textureMove = tickTime / 10d;
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
