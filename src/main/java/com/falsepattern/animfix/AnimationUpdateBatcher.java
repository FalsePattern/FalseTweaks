package com.falsepattern.animfix;

import lombok.SneakyThrows;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.texture.TextureMap;

import java.nio.IntBuffer;

public class AnimationUpdateBatcher {
    public static TextureMap currentAtlas = null;
    public static AnimationUpdateBatcher batcher = null;
    private final int mipLevels;
    private final int xOffset;
    private final int yOffset;
    private final int width;
    private final int height;
    private final IntBuffer[] data;

    public AnimationUpdateBatcher(int xOffset, int yOffset, int width, int height, int mipLevel) {
        this.mipLevels = mipLevel;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.width = width;
        this.height = height;
        data = new IntBuffer[mipLevel + 1];
        for (int i = 0; i <= mipLevel; i++) {
            data[i] = GLAllocation.createDirectIntBuffer((width >>> i) * (height >>> i));
        }
    }

    public boolean batchUpload(int[][] texture, int width, int height, int xOffset, int yOffset) {
        xOffset -= this.xOffset;
        yOffset -= this.yOffset;
        if (xOffset < 0 || xOffset >= this.width || yOffset < 0 || yOffset >= this.height) {
            return false;
        }
        int w = this.width;
        for (int mipMapLevel = 0; mipMapLevel < texture.length; mipMapLevel++) {
            IntBuffer atlasTexture = data[mipMapLevel];

            int base = yOffset * w + xOffset;
            for (int i = 0; i < height; i++) {
                atlasTexture.position(base + i * w);
                atlasTexture.put(texture[mipMapLevel], i * width, width);
            }
            xOffset >>>= 1;
            yOffset >>>= 1;
            width >>>= 1;
            height >>>= 1;
            w >>>= 1;
        }
        return true;
    }

    @SneakyThrows
    public void upload() {
        for (int i = 0; i <= mipLevels; i++) {
            IntBuffer buf = data[i];
            buf.position(0);
            GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, i, xOffset >>> i, yOffset >>> i, width >>> i, height >>> i,
                                 GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, buf);
        }
    }
}
