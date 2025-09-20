/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2025 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalseTweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * FalseTweaks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalseTweaks. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.falsetweaks.modules.cubicparticles;

import net.minecraft.client.renderer.Tessellator;

public class ParticleUtil {
    public static void drawCube(Tessellator tess,
                                float pr,
                                float pg,
                                float pb,
                                float u1,
                                float u2,
                                float v1,
                                float v2,
                                float scale,
                                float x,
                                float y,
                                float z) {
        float xp = x + scale;
        float xn = x - scale;
        float yp = y + scale;
        float yn = y - scale;
        float zp = z + scale;
        float zn = z - scale;
        setColor(tess, pr, pg, pb, 0.6f);
        drawXPos(tess, yn, yp, zn, zp, xp, u1, v1, u2, v2);
        drawXNeg(tess, yn, yp, zn, zp, xn, u1, v1, u2, v2);
        setColor(tess, pr, pg, pb, 1.0f);
        drawYPos(tess, xn, xp, zn, zp, yp, u1, v1, u2, v2);
        setColor(tess, pr, pg, pb, 0.5f);
        drawYNeg(tess, xn, xp, zn, zp, yn, u1, v1, u2, v2);
        setColor(tess, pr, pg, pb, 0.8f);
        drawZPos(tess, xn, xp, yn, yp, zp, u1, v1, u2, v2);
        drawZNeg(tess, xn, xp, yn, yp, zn, u1, v1, u2, v2);
    }

    private static void setColor(Tessellator tess, float pr, float pg, float pb, float luminosity) {
        tess.setColorOpaque_F(pr * luminosity, pg * luminosity, pb * luminosity);
    }

    private static void drawXPos(Tessellator tessellator,
                                 double minY,
                                 double maxY,
                                 double minZ,
                                 double maxZ,
                                 double x,
                                 double u1,
                                 double v1,
                                 double u2,
                                 double v2) {
        tessellator.addVertexWithUV(x, maxY, minZ, u2, v1);
        tessellator.addVertexWithUV(x, maxY, maxZ, u1, v1);
        tessellator.addVertexWithUV(x, minY, maxZ, u1, v2);
        tessellator.addVertexWithUV(x, minY, minZ, u2, v2);
    }

    private static void drawXNeg(Tessellator tessellator,
                                 double minY,
                                 double maxY,
                                 double minZ,
                                 double maxZ,
                                 double x,
                                 double u1,
                                 double v1,
                                 double u2,
                                 double v2) {
        tessellator.addVertexWithUV(x, maxY, maxZ, u2, v1);
        tessellator.addVertexWithUV(x, maxY, minZ, u1, v1);
        tessellator.addVertexWithUV(x, minY, minZ, u1, v2);
        tessellator.addVertexWithUV(x, minY, maxZ, u2, v2);
    }

    private static void drawYPos(Tessellator tessellator,
                                 double minX,
                                 double maxX,
                                 double minZ,
                                 double maxZ,
                                 double y,
                                 double u1,
                                 double v1,
                                 double u2,
                                 double v2) {
        tessellator.addVertexWithUV(minX, y, maxZ, u2, v1);
        tessellator.addVertexWithUV(maxX, y, maxZ, u1, v1);
        tessellator.addVertexWithUV(maxX, y, minZ, u1, v2);
        tessellator.addVertexWithUV(minX, y, minZ, u2, v2);
    }

    private static void drawYNeg(Tessellator tessellator,
                                 double minX,
                                 double maxX,
                                 double minZ,
                                 double maxZ,
                                 double y,
                                 double u1,
                                 double v1,
                                 double u2,
                                 double v2) {
        tessellator.addVertexWithUV(maxX, y, maxZ, u2, v1);
        tessellator.addVertexWithUV(minX, y, maxZ, u1, v1);
        tessellator.addVertexWithUV(minX, y, minZ, u1, v2);
        tessellator.addVertexWithUV(maxX, y, minZ, u2, v2);
    }

    private static void drawZPos(Tessellator tessellator,
                                 double minX,
                                 double maxX,
                                 double minY,
                                 double maxY,
                                 double z,
                                 double u1,
                                 double v1,
                                 double u2,
                                 double v2) {
        tessellator.addVertexWithUV(maxX, maxY, z, u2, v1);
        tessellator.addVertexWithUV(minX, maxY, z, u1, v1);
        tessellator.addVertexWithUV(minX, minY, z, u1, v2);
        tessellator.addVertexWithUV(maxX, minY, z, u2, v2);
    }

    private static void drawZNeg(Tessellator tessellator,
                                 double minX,
                                 double maxX,
                                 double minY,
                                 double maxY,
                                 double z,
                                 double u1,
                                 double v1,
                                 double u2,
                                 double v2) {
        tessellator.addVertexWithUV(minX, maxY, z, u2, v1);
        tessellator.addVertexWithUV(maxX, maxY, z, u1, v1);
        tessellator.addVertexWithUV(maxX, minY, z, u1, v2);
        tessellator.addVertexWithUV(minX, minY, z, u2, v2);
    }
}
