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

package com.falsepattern.falsetweaks.mixin.mixins.client.voxelizer;

import com.falsepattern.falsetweaks.config.VoxelizerConfig;
import com.falsepattern.falsetweaks.modules.voxelizer.Data;
import com.falsepattern.falsetweaks.modules.voxelizer.Layer;
import com.falsepattern.falsetweaks.modules.voxelizer.VoxelMesh;
import com.falsepattern.falsetweaks.modules.voxelizer.interfaces.ITextureAtlasSpriteMixin;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

@Mixin(TextureAtlasSprite.class)
@Accessors(fluent = true)
public abstract class TextureAtlasSpriteMixin implements ITextureAtlasSpriteMixin {
    @Getter
    @Shadow
    protected int frameCounter;

    @Getter
    @Shadow
    private boolean useAnisotropicFiltering;

    @Shadow public abstract int[][] getFrameTextureData(int p_147965_1_);

    @Shadow public abstract int getFrameCount();

    @Shadow public abstract int getIconWidth();

    @Shadow public abstract int getIconHeight();

    private VoxelMesh voxelMesh;

    @Inject(method = "clearFramesTextureData",
            at = @At(value = "HEAD"),
            require = 1)
    private void compileStatic(CallbackInfo ci) {
        voxelMesh = new VoxelMesh(VoxelizerConfig.MESH_OPTIMIZATION_STRATEGY_PRESET.strategy, new Layer((TextureAtlasSprite)(Object)this, 0.0625F));
        voxelMesh.compile();
    }

    @Inject(method = "getMinU",
            at = @At("HEAD"),
            require = 1)
    private void trackLastUsedSprite(CallbackInfoReturnable<Float> cir) {
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
    public int[][] getFrameTextureDataSafe(int id) {
        if (getFrameCount() == 0) {
            return null;
        }
        id %= getFrameCount();
        return getFrameTextureData(id);
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
