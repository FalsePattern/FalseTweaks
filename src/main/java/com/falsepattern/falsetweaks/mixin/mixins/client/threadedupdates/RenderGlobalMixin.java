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
package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates;

import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadSafeSettings;
import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadedChunkUpdateHelper;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public abstract class RenderGlobalMixin {
    @Shadow
    public Minecraft mc;

    @Inject(method = "loadRenderers",
            at = @At("HEAD"),
            require = 1)
    @SuppressWarnings("CastToIncompatibleInterface")
    private void updateFancyGraphics(CallbackInfo ci) {
        val settings = (ThreadSafeSettings) mc.gameSettings;
        settings.ft$update();
    }

    @Inject(method = "loadRenderers",
            at = @At("RETURN"),
            require = 1)
    private void doSpawnThreads(CallbackInfo ci) {
        ThreadedChunkUpdateHelper.instance.spawnThreads((RenderGlobal) (Object) this);
    }
}
