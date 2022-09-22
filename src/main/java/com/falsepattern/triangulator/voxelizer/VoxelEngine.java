/*
 * Triangulator
 *
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice, this permission notice and the word "SNEED"
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.triangulator.voxelizer;

import lombok.val;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

//Test code please ignore
public class VoxelEngine {
    public static void main(String[] args) throws IOException {
        val image1 = ImageIO.read(new File("diamond_sword.png"));
        val image2 = ImageIO.read(new File("rail_normal.png"));
        val texture1 = new TextureAtlasSprite("test") {};
        val texture2 = new TextureAtlasSprite("test") {};
        texture1.loadSprite(new BufferedImage[]{image1}, null, false);
        texture2.loadSprite(new BufferedImage[]{image2}, null, false);
        val layer1 = new Layer(texture1, 1);
        val layer2 = new Layer(texture2, 1);
        val voxels = new VoxelGrid(layer1, layer2);
        voxels.compile();
        for (int z = 0; z < voxels.zSize; z++) {
            val img = new BufferedImage(voxels.xSize * 150, voxels.ySize * 150, BufferedImage.TYPE_INT_RGB);
            val gfx = img.createGraphics();
            gfx.setColor(Color.WHITE);
            gfx.setStroke(new BasicStroke(5));
            gfx.fillRect(0, 0, img.getWidth(), img.getHeight());
            gfx.setColor(Color.BLUE);
            for (int y = 0; y < voxels.ySize; y++) {
                for (int x = 0; x < voxels.xSize; x++) {
                    for (val face : Dir.values()) {
                        if (!voxels.getFace(x, y, z, face)) {
                            continue;
                        }
                        int imgX = x * 100 + 100;
                        int imgY = y * 100 + 100;
                        int imgW = 10;
                        int imgH = 10;
                        switch (face) {
                            case Left:
                                imgH = 100;
                                break;
                            case Right:
                                imgX += 90;
                                imgH = 100;
                                break;
                            case Up:
                                imgW = 100;
                                break;
                            case Down:
                                imgY += 90;
                                imgW = 100;
                                break;
                            case In:
                                imgX += 20;
                                imgY += 20;
                                imgW = 60;
                                imgH = 60;
                                gfx.drawRect(imgX, imgY, imgW, imgH);
                                continue;
                            case Out:
                                imgX += 40;
                                imgY += 40;
                                imgW = 20;
                                imgH = 20;
                                gfx.drawRect(imgX, imgY, imgW, imgH);
                                continue;
                        }
                        gfx.fillRect(imgX, imgY, imgW, imgH);
                    }
                }
            }
            ImageIO.write(img, "PNG", new File("test" + z + ".png"));
        }
    }
}
