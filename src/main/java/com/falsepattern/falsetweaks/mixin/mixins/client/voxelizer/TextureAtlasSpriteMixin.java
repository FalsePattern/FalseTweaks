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

package com.falsepattern.falsetweaks.mixin.mixins.client.voxelizer;

import com.falsepattern.falsetweaks.api.ThreadedChunkUpdates;
import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadedChunkUpdateHelper;
import com.falsepattern.falsetweaks.modules.voxelizer.Data;
import com.falsepattern.falsetweaks.modules.voxelizer.Layer;
import com.falsepattern.falsetweaks.modules.voxelizer.VoxelMesh;
import com.falsepattern.falsetweaks.modules.voxelizer.interfaces.ITextureAtlasSpriteMixin;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

@Mixin(TextureAtlasSprite.class)
@Accessors(fluent = true,
           chain = false)
public abstract class TextureAtlasSpriteMixin implements ITextureAtlasSpriteMixin {
    @Getter
    @Shadow
    protected int frameCounter;

    @Getter
    @Shadow
    private boolean useAnisotropicFiltering;

    @Getter
    @Setter
    private Layer[] layers;
    private VoxelMesh voxelMesh;

    private byte[][] alphaData;

    @Shadow
    public abstract int[][] getFrameTextureData(int p_147965_1_);

    @Shadow
    public abstract int getFrameCount();

    @Shadow
    public abstract int getIconWidth();

    @Shadow
    public abstract int getIconHeight();

    @Inject(method = "clearFramesTextureData",
            at = @At(value = "HEAD"),
            require = 1)
    private void extractAlpha(CallbackInfo ci) {
        val frameCount = getFrameCount();
        alphaData = new byte[frameCount][];
        for (int i = 0; i < frameCount; i++) {
            val frameData = getFrameTextureData(i)[0];
            val alpha = new byte[frameData.length];
            for (int j = 0; j < frameData.length; j++) {
                alpha[j] = (byte) ((frameData[j] >>> 24) & 0xFF);
            }
            alphaData[i] = alpha;
        }
    }

    @Inject(method = "getMinU",
            at = @At("HEAD"),
            require = 1)
    private void trackLastUsedSprite(CallbackInfoReturnable<Float> cir) {
        if (ThreadedChunkUpdates.isEnabled() && !ThreadedChunkUpdateHelper.isMainThread()) {
            return;
        }
        Data.setLastUsedSprite((TextureAtlasSprite) (Object) this);
    }

    @Override
    public VoxelMesh getVoxelMesh() {
        return voxelMesh;
    }

    @Override
    public void setVoxelMesh(VoxelMesh mesh) {
        voxelMesh = mesh;
    }

    @Override
    public int getFrameAlphaData(int id, int x, int y) {
        if (alphaData == null || alphaData.length == 0) {
            val fc = getFrameCount();
            if (fc == 0) {
                return 255;
            }
            id %= fc;
            val textureData = getFrameTextureData(id);
            if (textureData == null) {
                return 255;
            } else {
                return (textureData[0][y * getIconWidth() + x] >>> 24) & 0xFF;
            }
        } else {
            id %= alphaData.length;
            return alphaData[id][y * getIconWidth() + x] & 0xFF;
        }
    }

    @Override
    public int getRealWidth() {
        return useAnisotropicFiltering ? getIconWidth() - 16 : getIconWidth();
    }

    @Override
    public int getRealHeight() {
        return useAnisotropicFiltering ? getIconHeight() - 16 : getIconHeight();
    }
}
