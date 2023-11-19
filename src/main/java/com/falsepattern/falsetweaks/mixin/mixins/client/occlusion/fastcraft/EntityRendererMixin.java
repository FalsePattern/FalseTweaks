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

package com.falsepattern.falsetweaks.mixin.mixins.client.occlusion.fastcraft;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.EntityLivingBase;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Dynamic
    @Redirect(method = "renderWorld",
              at = @At(value = "INVOKE",
                       target = "Lfastcraft/HC;t(Lnet/minecraft/client/renderer/RenderGlobal;Lnet/minecraft/entity/EntityLivingBase;Z)Z"),
              require = 1)
    private boolean unhookUpdateRenderers(RenderGlobal instance, EntityLivingBase entity, boolean bool) {
        return instance.updateRenderers(entity, bool);
    }
}
