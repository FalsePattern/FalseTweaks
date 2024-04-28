/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2024 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
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

package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates.optifine;

import com.falsepattern.falsetweaks.modules.threadedupdates.OptiFineCompat;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shadersmod.client.BlockAliases;
import shadersmod.client.Shaders;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;

@Mixin(value = Shaders.class,
       remap = false)
public abstract class ShadersMixin {
    @Inject(method = "uninit",
            at = @At("HEAD"),
            require = 1)
    private static void scheduleDeinit(CallbackInfo ci) {
        OptiFineCompat.scheduledReload = true;
    }

    /**
     * @author FalsePattern
     * @reason Thread Safety
     */
    @Overwrite
    public static int getEntityData() {
        val e = OptiFineCompat.ThreadSafeEntityData.TL.get();
        return e.entityData[e.entityDataIndex * 2];
    }

    /**
     * @author FalsePattern
     * @reason Thread Safety
     */
    @Overwrite
    public static int getEntityData2() {
        val e = OptiFineCompat.ThreadSafeEntityData.TL.get();
        return e.entityData[e.entityDataIndex * 2 + 1];
    }

    /**
     * @author FalsePattern
     * @reason Thread Safety
     */
    @Overwrite
    public static int setEntityData1(int data1) {
        val e = OptiFineCompat.ThreadSafeEntityData.TL.get();
        e.entityData[e.entityDataIndex * 2] = e.entityData[e.entityDataIndex * 2] & '\uffff' | data1 << 16;
        return data1;
    }

    /**
     * @author FalsePattern
     * @reason Thread Safety
     */
    @Overwrite
    public static int setEntityData2(int data2) {
        val e = OptiFineCompat.ThreadSafeEntityData.TL.get();
        e.entityData[e.entityDataIndex * 2 + 1] = e.entityData[e.entityDataIndex * 2 + 1] & -65536 | data2 & '\uffff';
        return data2;
    }

    /**
     * @author FalsePattern
     * @reason Thread Safety
     */
    @Overwrite
    public static void pushEntity(int data0, int data1) {
        val e = OptiFineCompat.ThreadSafeEntityData.TL.get();
        ++e.entityDataIndex;
        e.entityData[e.entityDataIndex * 2] = data0 & '\uffff' | data1 << 16;
        e.entityData[e.entityDataIndex * 2 + 1] = 0;
    }

    /**
     * @author FalsePattern
     * @reason Thread Safety
     */
    @Overwrite
    public static void pushEntity(int data0) {
        val e = OptiFineCompat.ThreadSafeEntityData.TL.get();
        ++e.entityDataIndex;
        e.entityData[e.entityDataIndex * 2] = data0 & '\uffff';
        e.entityData[e.entityDataIndex * 2 + 1] = 0;
    }

    /**
     * @author FalsePattern
     * @reason Thread Safety
     */
    @Overwrite
    public static void pushEntity(Block block) {
        val e = OptiFineCompat.ThreadSafeEntityData.TL.get();
        int blockId = Block.blockRegistry.getIDForObject(block);
        int metadata = 0;
        blockId = BlockAliases.getMappedBlockId(blockId, metadata);
        ++e.entityDataIndex;
        e.entityData[e.entityDataIndex * 2] = blockId & '\uffff' | block.getRenderType() << 16;
        e.entityData[e.entityDataIndex * 2 + 1] = metadata;
    }

    /**
     * @author FalsePattern
     * @reason Thread Safety
     */
    @Overwrite
    public static void pushEntity(RenderBlocks rb, Block block, int x, int y, int z) {
        val e = OptiFineCompat.ThreadSafeEntityData.TL.get();
        int blockId = Block.blockRegistry.getIDForObject(block);
        int metadata = rb.blockAccess.getBlockMetadata(x, y, z);
        blockId = BlockAliases.getMappedBlockId(blockId, metadata);
        ++e.entityDataIndex;
        e.entityData[e.entityDataIndex * 2] = blockId & '\uffff' | block.getRenderType() << 16;
        e.entityData[e.entityDataIndex * 2 + 1] = metadata;
    }

    /**
     * @author FalsePattern
     * @reason Thread Safety
     */
    @Overwrite
    public static void popEntity() {
        val e = OptiFineCompat.ThreadSafeEntityData.TL.get();
        e.entityData[e.entityDataIndex * 2] = 0;
        e.entityData[e.entityDataIndex * 2 + 1] = 0;
        --e.entityDataIndex;
    }
}
