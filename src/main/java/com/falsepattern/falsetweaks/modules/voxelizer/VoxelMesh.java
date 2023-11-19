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

package com.falsepattern.falsetweaks.modules.voxelizer;

import com.falsepattern.falsetweaks.Share;
import com.falsepattern.falsetweaks.config.VoxelizerConfig;
import com.falsepattern.falsetweaks.modules.voxelizer.interfaces.ITextureAtlasSpriteMixin;
import com.falsepattern.falsetweaks.modules.voxelizer.strategy.MergingStrategy;
import com.falsepattern.lib.util.MathUtil;
import lombok.val;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class VoxelMesh {
    public static final float EPSILON = 0.0001f;
    private static final Matrix4fc IDENTITY = new Matrix4f();
    private static final ThreadLocal<Vector3f> workingVector = ThreadLocal.withInitial(Vector3f::new);
    public final float totalThickness;
    private final MergingStrategy strategy;
    private final Layer[] layers;
    private final float[] xOffsets;
    private final float[] yOffsets;
    private final float[] zOffsets;
    private final VoxelCompiler compiler;
    private Map<VoxelType, List<Face>> faceCache;
    private String cacheIdentity = null;

    public VoxelMesh(MergingStrategy strategy, Layer... layers) {
        this.strategy = strategy;
        this.layers = layers;
        this.compiler = new VoxelCompiler(layers);
        xOffsets = new float[compiler.xSize + 1];
        yOffsets = new float[compiler.ySize + 1];
        zOffsets = new float[compiler.zSize + 1];
        float offset = 0;
        for (int z = compiler.zSize - 1; z >= 0; z--) {
            zOffsets[z + 1] = offset;
            offset -= layers[z].thickness;
        }
        zOffsets[0] = offset;
        totalThickness = offset;
        for (int x = 0; x <= compiler.xSize; x++) {
            xOffsets[x] = 1 - x / (float) compiler.xSize;
        }
        for (int y = 0; y <= compiler.ySize; y++) {
            yOffsets[y] = 1 - y / (float) compiler.ySize;
        }
    }

    public static VoxelMesh getMesh(TextureAtlasSprite iicon) {
        val texture = (ITextureAtlasSpriteMixin) iicon;
        VoxelMesh mesh = texture.getVoxelMesh();
        if (mesh == null) {
            val layers = texture.layers();
            mesh = new VoxelMesh(VoxelizerConfig.MESH_OPTIMIZATION_STRATEGY_PRESET.strategy,
                                 layers == null ? new Layer[]{new Layer(iicon, 0.0625F)} : layers);
            texture.setVoxelMesh(mesh);
        }
        return mesh;
    }

    private static void setNormal(Tessellator tess, Vector3f normal) {
        tess.setNormal(normal.x, normal.y, normal.z);
    }

    private static void setWorldSpaceLight(Tessellator tess, Vector3f normal) {
        float up = MathUtil.clamp(normal.dot(0, 1, 0), 0, 1);
        float down = MathUtil.clamp(normal.dot(0, -1, 0), 0, 1);
        float northsouth = MathUtil.clamp(MathUtil.abs(normal.dot(0, 0, 1)), 0, 1);
        float eastwest = MathUtil.clamp(MathUtil.abs(normal.dot(1, 0, 0)), 0, 1);
        float light = Math.max(Math.max(up, 0.5f * down), Math.max(0.8f * northsouth, 0.6f * eastwest));
        tess.setColorOpaque_F(light, light, light);
    }

    private static void setupLighting(Tessellator tess, Vector3f normal, boolean chunkSpace, Matrix4fc transform) {
        transform.transformDirection(normal);
        if (chunkSpace) {
            setWorldSpaceLight(tess, normal);
        } else {
            setNormal(tess, normal);
        }
    }

    private static void addVertexWithUVWithTransform(Tessellator tess, Vector3f pos, float u, float v, Matrix4fc transform) {
        transform.transformPosition(pos);
        tess.addVertexWithUV(pos.x, pos.y, pos.z, u, v);
    }

    public int xSize() {
        return compiler.xSize;
    }

    public int ySize() {
        return compiler.ySize;
    }

    public int zSize() {
        return compiler.zSize;
    }

    public void renderToTessellator(Tessellator tess, int overlayLayer, boolean remapUV, VoxelType type) {
        renderToTessellator(tess, overlayLayer, remapUV, false, IDENTITY, null, type);
    }

    public void renderToTessellator(Tessellator tess, int overlayLayer, boolean remapUV, boolean chunkSpace, Matrix4fc transform, Function<Face, Boolean> trimmingFunction, VoxelType type) {
        compile();
        val vec = workingVector.get();
        for (val face : faceCache.get(type)) {
            if (trimmingFunction != null && trimmingFunction.apply(face)) {
                continue;
            }
            float u1, v1, u2, v2;
            float EPSILON_OUT = overlayLayer * EPSILON;
            if (remapUV) {
                u1 = xOffsets[face.minX];
                v1 = yOffsets[face.minY];
                u2 = xOffsets[face.maxX + 1];
                v2 = yOffsets[face.maxY + 1];
            } else {
                u1 = face.u1;
                v1 = face.v1;
                u2 = face.u2;
                v2 = face.v2;
            }
            switch (face.dir) {
                // @formatter:off
                case Front: {
                    setupLighting(tess, vec.set(0, 0, 1), chunkSpace, transform);
                    addVertexWithUVWithTransform(tess, vec.set(xOffsets[face.minX] + EPSILON, yOffsets[face.maxY + 1] - EPSILON, zOffsets[face.z + 1] + EPSILON_OUT), u1, v2, transform);
                    addVertexWithUVWithTransform(tess, vec.set(xOffsets[face.minX] + EPSILON, yOffsets[face.minY] + EPSILON, zOffsets[face.z + 1] + EPSILON_OUT), u1, v1, transform);
                    addVertexWithUVWithTransform(tess, vec.set(xOffsets[face.maxX + 1] - EPSILON, yOffsets[face.minY] + EPSILON, zOffsets[face.z + 1] + EPSILON_OUT), u2, v1, transform);
                    addVertexWithUVWithTransform(tess, vec.set(xOffsets[face.maxX + 1] - EPSILON, yOffsets[face.maxY + 1] - EPSILON, zOffsets[face.z + 1] + EPSILON_OUT), u2, v2, transform);
                    break;
                }
                case Back: {
                    setupLighting(tess, vec.set(0, 0, -1), chunkSpace, transform);
                    addVertexWithUVWithTransform(tess, vec.set(xOffsets[face.maxX + 1] - EPSILON, yOffsets[face.minY] + EPSILON, zOffsets[face.z] - EPSILON_OUT), u2, v1, transform);
                    addVertexWithUVWithTransform(tess, vec.set(xOffsets[face.minX] + EPSILON, yOffsets[face.minY] + EPSILON, zOffsets[face.z] - EPSILON_OUT), u1, v1, transform);
                    addVertexWithUVWithTransform(tess, vec.set(xOffsets[face.minX] + EPSILON, yOffsets[face.maxY + 1] - EPSILON, zOffsets[face.z] - EPSILON_OUT), u1, v2, transform);
                    addVertexWithUVWithTransform(tess, vec.set(xOffsets[face.maxX + 1] - EPSILON, yOffsets[face.maxY + 1] - EPSILON, zOffsets[face.z] - EPSILON_OUT), u2, v2, transform);
                    break;
                }
                case Left: {
                    setupLighting(tess, vec.set(1, 0, 0), chunkSpace, transform);
                    addVertexWithUVWithTransform(tess, vec.set(xOffsets[face.minX] - EPSILON + EPSILON_OUT, yOffsets[face.maxY + 1] - EPSILON - EPSILON_OUT, zOffsets[face.z + 1] + EPSILON + EPSILON_OUT), u1, v2, transform);
                    addVertexWithUVWithTransform(tess, vec.set(xOffsets[face.minX] - EPSILON + EPSILON_OUT, yOffsets[face.maxY + 1] - EPSILON - EPSILON_OUT, zOffsets[face.z] - EPSILON - EPSILON_OUT), u2, v2, transform);
                    addVertexWithUVWithTransform(tess, vec.set(xOffsets[face.minX] - EPSILON + EPSILON_OUT, yOffsets[face.minY] + EPSILON + EPSILON_OUT, zOffsets[face.z] - EPSILON - EPSILON_OUT), u2, v1, transform);
                    addVertexWithUVWithTransform(tess, vec.set(xOffsets[face.minX] - EPSILON + EPSILON_OUT, yOffsets[face.minY] + EPSILON + EPSILON_OUT, zOffsets[face.z + 1] + EPSILON + EPSILON_OUT), u1, v1, transform);
                    break;
                }
                case Right: {
                    setupLighting(tess, vec.set(-1, 0, 0), chunkSpace, transform);
                    addVertexWithUVWithTransform(tess, vec.set(xOffsets[face.maxX + 1] + EPSILON - EPSILON_OUT, yOffsets[face.minY] + EPSILON + EPSILON_OUT, zOffsets[face.z + 1] + EPSILON + EPSILON_OUT), u1, v1, transform);
                    addVertexWithUVWithTransform(tess, vec.set(xOffsets[face.maxX + 1] + EPSILON - EPSILON_OUT, yOffsets[face.minY] + EPSILON + EPSILON_OUT, zOffsets[face.z] - EPSILON - EPSILON_OUT), u2, v1, transform);
                    addVertexWithUVWithTransform(tess, vec.set(xOffsets[face.maxX + 1] + EPSILON - EPSILON_OUT, yOffsets[face.maxY + 1] - EPSILON - EPSILON_OUT, zOffsets[face.z] - EPSILON - EPSILON_OUT), u2, v2, transform);
                    addVertexWithUVWithTransform(tess, vec.set(xOffsets[face.maxX + 1] + EPSILON - EPSILON_OUT, yOffsets[face.maxY + 1] - EPSILON - EPSILON_OUT, zOffsets[face.z + 1] + EPSILON + EPSILON_OUT), u1, v2, transform);
                    break;
                }
                case Up: {
                    setupLighting(tess, vec.set(0, 1, 0), chunkSpace, transform);
                    addVertexWithUVWithTransform(tess, vec.set(xOffsets[face.minX] + EPSILON + EPSILON_OUT, yOffsets[face.minY] - EPSILON + EPSILON_OUT, zOffsets[face.z + 1] + EPSILON + EPSILON_OUT), u1, v1, transform);
                    addVertexWithUVWithTransform(tess, vec.set(xOffsets[face.minX] + EPSILON + EPSILON_OUT, yOffsets[face.minY] - EPSILON + EPSILON_OUT, zOffsets[face.z] - EPSILON - EPSILON_OUT), u1, v2, transform);
                    addVertexWithUVWithTransform(tess, vec.set(xOffsets[face.maxX + 1] - EPSILON - EPSILON_OUT, yOffsets[face.minY] - EPSILON + EPSILON_OUT, zOffsets[face.z] - EPSILON - EPSILON_OUT), u2, v2, transform);
                    addVertexWithUVWithTransform(tess, vec.set(xOffsets[face.maxX + 1] - EPSILON - EPSILON_OUT, yOffsets[face.minY] - EPSILON + EPSILON_OUT, zOffsets[face.z + 1] + EPSILON + EPSILON_OUT), u2, v1, transform);
                    break;
                }
                case Down: {
                    setupLighting(tess, vec.set(0, -1, 0), chunkSpace, transform);
                    addVertexWithUVWithTransform(tess, vec.set(xOffsets[face.maxX + 1] - EPSILON - EPSILON_OUT, yOffsets[face.maxY + 1] + EPSILON - EPSILON_OUT, zOffsets[face.z + 1] + EPSILON + EPSILON_OUT), u2, v1, transform);
                    addVertexWithUVWithTransform(tess, vec.set(xOffsets[face.maxX + 1] - EPSILON - EPSILON_OUT, yOffsets[face.maxY + 1] + EPSILON - EPSILON_OUT, zOffsets[face.z] - EPSILON - EPSILON_OUT), u2, v2, transform);
                    addVertexWithUVWithTransform(tess, vec.set(xOffsets[face.minX] + EPSILON + EPSILON_OUT, yOffsets[face.maxY + 1] + EPSILON - EPSILON_OUT, zOffsets[face.z] - EPSILON - EPSILON_OUT), u1, v2, transform);
                    addVertexWithUVWithTransform(tess, vec.set(xOffsets[face.minX] + EPSILON + EPSILON_OUT, yOffsets[face.maxY + 1] + EPSILON - EPSILON_OUT, zOffsets[face.z + 1] + EPSILON + EPSILON_OUT), u1, v1, transform);
                    break;
                }
                // @formatter:on
            }
        }
    }

    public void compile() {
        String currentIdentity = getIdentity(0, false);
        if (!Objects.equals(cacheIdentity, currentIdentity)) {
            if (VoxelizerConfig.DEBUG_MESH_COMPILATION) {
                Share.log.info("Starting compilation for mesh \"" + currentIdentity + "\"");
            }
            faceCache = compiler.compile(strategy);
            if (VoxelizerConfig.DEBUG_MESH_COMPILATION) {
                Share.log.info("Compiled mesh \"" + currentIdentity + "\" with " + faceCache.size() + " faces!");
            }
            cacheIdentity = currentIdentity;
        }
    }

    public String getIdentity(int overlayLayer, boolean remapUV) {
        val result = new StringBuilder();
        if (remapUV) {
            result.append("remap_uv!");
        }
        if (overlayLayer > 0) {
            result.append("overlay").append(overlayLayer).append("!");
        }
        for (val layer : layers) {
            result.append(layer.textureIdentity()).append('&');
        }
        return result.toString();
    }

}
