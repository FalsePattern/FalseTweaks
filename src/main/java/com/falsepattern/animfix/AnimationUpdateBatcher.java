package com.falsepattern.animfix;

import lombok.SneakyThrows;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;

public class AnimationUpdateBatcher {
    public static TextureMap currentAtlas = null;
    public static AnimationUpdateBatcher batcher = null;
    private final int[] xOffset;
    private final int[] yOffset;
    private final int[] width;
    private final int[] height;
    private final int[][] data;
    public AnimationUpdateBatcher(int xOffset, int yOffset, int width, int height, int mipLevel) {
        this.xOffset = new int[mipLevel + 1];
        this.yOffset = new int[mipLevel + 1];
        this.width = new int[mipLevel + 1];
        this.height = new int[mipLevel + 1];
        data = new int[mipLevel + 1][];
        for (int i = 0; i <= mipLevel; i++) {
            this.xOffset[i] = (xOffset >>> i);
            this.yOffset[i] = (yOffset >>> i);
            this.width[i] = (width >>> i);
            this.height[i] = (height >>> i);
            data[i] = new int[this.width[i] * this.height[i]];
        }
    }

    public boolean batchUpload(int mipMapLevel, int[] texture, int width, int height, int xOffset, int yOffset) {
        int atlasX = this.xOffset[mipMapLevel];
        int atlasY = this.yOffset[mipMapLevel];
        int atlasWidth = this.width[mipMapLevel];
        int atlasHeight = this.height[mipMapLevel];
        xOffset -= atlasX;
        yOffset -= atlasY;
        if (xOffset < 0 || xOffset >= atlasWidth || yOffset < 0 || yOffset >= atlasHeight) return false;
        int[] atlasTexture = data[mipMapLevel];

        int base = yOffset * atlasWidth + xOffset;
        for (int i = 0; i < height; i++) {
            System.arraycopy(texture, i * width, atlasTexture, base + i * atlasWidth, width);
        }
        return true;
    }

    @SneakyThrows
    public void upload() {
        TextureUtil.uploadTextureMipmap(data, width[0], height[0], xOffset[0], yOffset[0], false, false);
    }
}
