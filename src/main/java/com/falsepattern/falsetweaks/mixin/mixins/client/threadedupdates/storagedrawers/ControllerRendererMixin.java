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

package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates.storagedrawers;

import com.falsepattern.falsetweaks.modules.threadedupdates.interop.StorageDrawersCompat;
import com.jaquadro.minecraft.storagedrawers.client.renderer.ControllerRenderer;
import com.jaquadro.minecraft.storagedrawers.util.RenderHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ControllerRenderer.class,
       remap = false)
public abstract class ControllerRendererMixin {
    @Redirect(method = {
            "renderInventoryBlock(Lcom/jaquadro/minecraft/storagedrawers/block/BlockController;IILnet/minecraft/client/renderer/RenderBlocks;)V",
            "renderWorldBlock(Lnet/minecraft/world/IBlockAccess;IIILcom/jaquadro/minecraft/storagedrawers/block/BlockController;ILnet/minecraft/client/renderer/RenderBlocks;)Z"},
              at = @At(value = "FIELD",
                       target = "Lcom/jaquadro/minecraft/storagedrawers/util/RenderHelper;instance:Lcom/jaquadro/minecraft/storagedrawers/util/RenderHelper;"),
              require = 4)
    private RenderHelper threadSafeGet() {
        return StorageDrawersCompat.instances.get();
    }
}
