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
package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates.opencomputers;

import com.falsepattern.falsetweaks.modules.threading.MainThreadContainer;
import li.cil.oc.util.RenderState$;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RenderState$.class,
       remap = false)
public abstract class RenderStateMixin {
    @Redirect(method = "checkError",
              at = @At(value = "INVOKE",
                       target = "Lorg/lwjgl/opengl/GL11;glGetError()I"),
              require = 1)
    private int noErrorOffMainThread() {
        if (MainThreadContainer.isMainThread()) {
            return GL11.glGetError();
        }
        return GL11.GL_NO_ERROR;
    }
}
