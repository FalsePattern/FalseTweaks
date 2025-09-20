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

package com.falsepattern.falsetweaks.mixin.mixins.client.misc;

import com.falsepattern.falsetweaks.modules.misc.ItemTexturingManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

@Mixin(RenderGlobal.class)
public class FastItemTexturing_RenderGlobalMixin {
    @Inject(method = "renderEntities",
            at = @At(value = "INVOKE_STRING",
                     target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V",
                     args = "ldc=entities"),
            require = 1)
    private void beginEntityLoop(EntityLivingBase pos, ICamera frustrum, float partialTickTime, CallbackInfo ci) {
        ItemTexturingManager.beginEntities();
    }

    @Inject(method = "renderEntities",
            at = @At(value = "INVOKE_STRING",
                     target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V",
                     args = "ldc=blockentities"),
            require = 1)
    private void endEntityLoop(EntityLivingBase pos, ICamera frustrum, float partialTickTime, CallbackInfo ci) {
        ItemTexturingManager.endEntities();
    }

    @WrapOperation(method = "renderEntities",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/entity/RenderManager;renderEntitySimple(Lnet/minecraft/entity/Entity;F)Z"),
                   slice = @Slice(from = @At(value = "INVOKE_STRING",
                                             target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V",
                                             args = "ldc=entities")),
                   require = 1)
    private boolean preDrawEntity(RenderManager instance, Entity entity, float partialTicks, Operation<Boolean> original) {
        ItemTexturingManager.beforeEntity(entity);
        return original.call(instance, entity, partialTicks);
    }
}
