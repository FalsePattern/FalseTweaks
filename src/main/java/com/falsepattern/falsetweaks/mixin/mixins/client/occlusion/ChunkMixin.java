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

package com.falsepattern.falsetweaks.mixin.mixins.client.occlusion;

import com.falsepattern.falsetweaks.modules.occlusion.ChunkThread;
import com.falsepattern.falsetweaks.modules.occlusion.ICulledChunk;
import com.falsepattern.falsetweaks.modules.occlusion.OcclusionHelpers;
import com.falsepattern.falsetweaks.modules.occlusion.VisGraph;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

@Mixin(Chunk.class)
public abstract class ChunkMixin implements ICulledChunk {
    @Shadow
    public abstract Block getBlock(int p_150810_1_, int p_150810_2_, int p_150810_3_);

    @Shadow
    public World worldObj;
    @Shadow
    @Final
    public int xPosition;
    @Shadow
    @Final
    public int zPosition;
    private VisGraph[] visibility;

    private static ChunkThread worker = new ChunkThread();

    static {
        worker.start();
    }

    public Chunk buildCulledSides() {
        if (!this.worldObj.getChunkProvider().chunkExists(xPosition, zPosition)) {
            return null;
        }
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                for (int y = 0; y < 256; ++y) {
                    checkPosSolid(i, y, j, null);
                }
            }
        }
        OcclusionHelpers.updateArea(xPosition * 16 - 1, 0, zPosition * 16 - 1, xPosition * 16 + 16, 255, zPosition * 16 + 16);
        return (Chunk) (Object) this;
    }


    @Inject(method = "<init>(Lnet/minecraft/world/World;II)V",
            at = @At("RETURN"),
            require = 1)
    private void onInit(World p_i1995_1_, int p_i1995_2_, int p_i1995_3_, CallbackInfo ci) {
        visibility = new VisGraph[16];
        for (int i = 0; i < 16; ++i) {
            visibility[i] = new VisGraph();
        }
    }

    @Override
    public VisGraph[] getVisibility() {
        return visibility;
    }

    boolean checkPosSolid(int x, int y, int z, Block block) {

        if (y > 255 || y < 0) {
            return false;
        }
        if (block == null) {
            block = getBlock(x, y, z);
        }
        VisGraph chunk = this.visibility[y >> 4];
        y &= 15;

        chunk.setOpaque(x, y, z, block.isOpaqueCube());
        return chunk.isDirty();
    }

    @Inject(method = "func_150807_a",
            at = @At("RETURN"),
            require = 1)
    private void onSetBlock(int x, int y, int z, Block block, int meta, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && this.worldObj.isRemote && checkPosSolid(x & 15, y, z & 15, block)) {
            worker.modified.add((Chunk) (Object) this);
        }
    }

    @Inject(method = "fillChunk",
            at = @At("RETURN"),
            require = 1)
    private void onFillChunk(byte[] p_76607_1_, int p_76607_2_, int p_76607_3_, boolean p_76607_4_, CallbackInfo ci) {
        worker.loaded.add((Chunk) (Object) this);
    }

}
