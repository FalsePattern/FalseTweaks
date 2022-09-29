/*
 * FalseTweaks
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

package com.falsepattern.falsetweaks.modules.voxelizer;

import com.falsepattern.falsetweaks.modules.voxelizer.strategy.MergingStrategy;
import lombok.val;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VoxelCompiler {
    public final int xSize;
    public final int ySize;
    public final int zSize;

    private final Layer[] layers;
    public VoxelCompiler(Layer... layers) {
        this.layers = Arrays.copyOf(layers, layers.length);
        {
            int x = 0, y = 0;
            zSize = this.layers.length;
            for (int i = 0; i < zSize; i++) {
                TextureAtlasSprite tex = layers[i].texture;
                x = Math.max(x, tex.getIconWidth());
                y = Math.max(y, tex.getIconHeight());
            }
            xSize = x;
            ySize = y;
        }
    }

    public List<Face> compile(MergingStrategy strategy) {
        val voxels = new VoxelGrid(xSize, ySize, zSize);
        for (int z = 0; z < zSize; z++)
            for (int y = 0; y < ySize; y++)
                for (int x = 0; x < xSize; x++) {
                    int alpha = layers[z].fetchAlpha(x, y, xSize, ySize);
                    voxels.setType(x, y, z, VoxelType.fromAlpha(alpha));
                }

        for (int z = -1; z < zSize; z++)
            for (int y = -1; y < ySize; y++)
                for (int x = -1; x < xSize; x++) {
                    int thisIndex = voxels.toIndex(x, y, z);
                    voxels.exchangeFaces(thisIndex, voxels.toIndex(x + 1, y, z), Dir.Right);
                    voxels.exchangeFaces(thisIndex, voxels.toIndex(x, y + 1, z), Dir.Down);
                    voxels.exchangeFaces(thisIndex, voxels.toIndex(x, y, z + 1), Dir.Front);
                }
        List<Face> results = new ArrayList<>();
        //Allocate earlier for reuse
        Face[][] front = new Face[ySize][xSize];
        Face[][] back = new Face[ySize][xSize];
        Face[] top = new Face[xSize];
        Face[] bottom = new Face[xSize];
        Face[] left = new Face[ySize];
        Face[] right = new Face[ySize];

        val faceBuilder = Face.builder();
        for (int z = 0; z < zSize; z++) {
            faceBuilder.z(z);
            Layer layer = layers[z];
            for (val type: VoxelType.renderable()) {
                //Front and back faces (2D merge)
                //Plus top and bottom faces inlined into the loop
                {
                    for (int y = 0; y < ySize; y++) {
                        faceBuilder.minY(y).maxY(y);
                        Face[] rowFront = front[y];
                        Face[] rowBack = back[y];
                        Arrays.fill(rowFront, null);
                        Arrays.fill(rowBack, null);
                        Arrays.fill(top, null);
                        Arrays.fill(bottom, null);
                        for (int x = 0; x < xSize; x++) {
                            if (voxels.getType(x, y, z) != type) {
                                continue;
                            }
                            //Front and Back
                            faceBuilder.minX(x)
                                       .maxX(x)
                                       .u1(layer.fetchU(x + 0.01f, xSize))
                                       .u2(layer.fetchU(x + 0.99f, xSize))
                                       .v1(layer.fetchV(y + 0.01f, ySize))
                                       .v2(layer.fetchV(y + 0.99f, ySize));
                            if (voxels.getFace(x, y, z, Dir.Front)) {
                                rowFront[x] = faceBuilder.dir(Dir.Front).build();
                            }
                            if (voxels.getFace(x, y, z, Dir.Back)) {
                                rowBack[x] = faceBuilder.dir(Dir.Back).build();
                            }
                            //Top and Bottom
                            faceBuilder.v1(layer.fetchV(y + 0.5f, ySize)).v2(layer.fetchV(y + 0.5f, ySize));
                            if (voxels.getFace(x, y, z, Dir.Up)) {
                                top[x] = faceBuilder.dir(Dir.Up).build();
                            }
                            if (voxels.getFace(x, y, z, Dir.Down)) {
                                bottom[x] = faceBuilder.dir(Dir.Down).build();
                            }
                        }
                        strategy.mergeSide(top);
                        strategy.mergeSide(bottom);
                        unwrap(top, results);
                        unwrap(bottom, results);
                    }
                    strategy.merge(back);
                    strategy.merge(front);
                    unwrap(back, results);
                    unwrap(front, results);
                }
                //Left and Right faces
                for (int x = 0; x < xSize; x++) {
                    Arrays.fill(left, null);
                    Arrays.fill(right, null);
                    faceBuilder.minX(x).maxX(x);
                    for (int y = 0; y < ySize; y++) {
                        if (voxels.getType(x, y, z) != type) {
                            continue;
                        }
                        faceBuilder.minY(y)
                                   .maxY(y)
                                   .u1(layer.fetchU(x + 0.5f, xSize))
                                   .u2(layer.fetchU(x + 0.5f, xSize))
                                   .v1(layer.fetchV(y + 0.01f, ySize))
                                   .v2(layer.fetchV(y + 0.99f, ySize));

                        if (voxels.getFace(x, y, z, Dir.Left)) {
                            left[y] = faceBuilder.dir(Dir.Left).build();
                        }
                        if (voxels.getFace(x, y, z, Dir.Right)) {
                            right[y] = faceBuilder.dir(Dir.Right).build();
                        }
                    }
                    strategy.mergeSide(left);
                    strategy.mergeSide(right);
                    unwrap(left, results);
                    unwrap(right, results);
                }
            }
        }
        return results;
    }

    private static void unwrap(Face[][] faces, List<Face> result) {
        for (Face[] row : faces)
            unwrap(row, result);
    }

    private static void unwrap(Face[] faces, List<Face> result) {
        for (Face f: faces)
            if (f != null && f.parent == null && !f.used) {
                result.add(f);
                f.used = true;
            }
    }
}
