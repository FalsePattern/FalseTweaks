package com.falsepattern.falsetweaks.modules.rendersafety;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.OpenGlHelper;

public class SafetyUtil {
    public static void pre(boolean enable) {
        if (enable) {
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        }
    }

    public static void post(boolean enable) {
        if (enable) {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
            GL11.glPopAttrib();
        }
    }
}
