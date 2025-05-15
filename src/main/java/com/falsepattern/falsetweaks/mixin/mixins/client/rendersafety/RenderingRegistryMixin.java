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

package com.falsepattern.falsetweaks.mixin.mixins.client.rendersafety;

import com.falsepattern.falsetweaks.config.RenderingSafetyConfig;
import com.falsepattern.falsetweaks.modules.rendersafety.SafetyUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lombok.val;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

@Mixin(value = RenderingRegistry.class,
       remap = false)
public abstract class RenderingRegistryMixin {
    @WrapOperation(method = "renderInventoryBlock",
                   at = @At(value = "INVOKE",
                       target = "Lcpw/mods/fml/client/registry/ISimpleBlockRenderingHandler;renderInventoryBlock(Lnet/minecraft/block/Block;IILnet/minecraft/client/renderer/RenderBlocks;)V"),
                   require = 1)
    private void wrapBlock(ISimpleBlockRenderingHandler instance, Block block, int metadata, int modelID, RenderBlocks renderer, Operation<Void> original) {
        val enable = RenderingSafetyConfig.ENABLE_BLOCK;
        SafetyUtil.pre(enable);
        original.call(instance, block, metadata, modelID, renderer);
        SafetyUtil.post(enable);
    }
}
