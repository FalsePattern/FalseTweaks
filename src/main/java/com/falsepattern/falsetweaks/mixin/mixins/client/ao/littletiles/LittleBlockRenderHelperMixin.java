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

package com.falsepattern.falsetweaks.mixin.mixins.client.ao.littletiles;

import com.creativemd.littletiles.client.render.LittleBlockRenderHelper;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(value = LittleBlockRenderHelper.class,
       remap = false)
public abstract class LittleBlockRenderHelperMixin {
    @ModifyConstant(method = "renderBlock",
                    constant = @Constant(intValue = 1,
                                         ordinal = 0),
                    slice = @Slice(from = @At(value = "INVOKE",
                                              target = "Lnet/minecraft/client/Minecraft;getMinecraft()Lnet/minecraft/client/Minecraft;",
                                              ordinal = 0)),
                    require = 1)
    private static int noNeedThread(int constant) {
        return 0;
    }
    @Redirect(method = "renderBlock",
              at = @At(value = "INVOKE",
                       target = "Lcom/creativemd/littletiles/common/utils/LittleTile;canBlockBeThreaded()Z"),
              require = 1)
    private static boolean forceNonThreaded(LittleTile instance) {
        return false;
    }

    @Redirect(method = "renderBlock",
              at = @At(value = "FIELD",
                       target = "Lcom/creativemd/littletiles/common/tileentity/TileEntityLittleTiles;needFullRenderUpdate:Z",
                       opcode = Opcodes.GETFIELD),
              require = 1)
    private static boolean forceNoNeedThread(TileEntityLittleTiles instance) {
        return false;
    }
}
