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
package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates;

import com.falsepattern.falsetweaks.modules.threadedupdates.saftey.ThreadSafeSettings;
import com.falsepattern.falsetweaks.modules.threading.FastThreadLocal;
import com.falsepattern.falsetweaks.modules.threading.MainThreadContainer;
import lombok.var;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.settings.GameSettings;

@Mixin(GameSettings.class)
public abstract class GameSettingsMixin implements ThreadSafeSettings {
    @Unique
    private final Object ft$updateLock = new Object();

    @Unique
    private volatile boolean ft$initialFancyGraphics;
    @Unique
    private volatile boolean ft$safeFancyGraphics;
    @Unique
    private volatile FastThreadLocal.DynamicValue<Boolean> ft$threadedFancyGraphics = new FastThreadLocal.DynamicValue<>();

    @Inject(method = "<init>*",
            at = @At("RETURN"),
            require = 1)
    private void init(CallbackInfo ci) {
        ft$update();
    }

    @Override
    public void ft$update() {
        if (!MainThreadContainer.isMainThread()) {
            throw new AssertionError("What made you think this was a good idea?");
        }

        synchronized (ft$updateLock) {
            ft$initialFancyGraphics = ft$safeFancyGraphics;
            ft$threadedFancyGraphics = new FastThreadLocal.DynamicValue<>();
        }
    }

    @Override
    public void ft$fancyGraphics(boolean fancyGraphics) {
        if (MainThreadContainer.isMainThread()) {
            ft$safeFancyGraphics = fancyGraphics;
            return;
        }

        synchronized (ft$updateLock) {
            ft$threadedFancyGraphics.set(fancyGraphics);
        }
    }

    @Override
    public boolean ft$fancyGraphics() {
        if (MainThreadContainer.isMainThread()) {
            return ft$safeFancyGraphics;
        }

        synchronized (ft$updateLock) {
            var threadFancyGraphics = ft$threadedFancyGraphics.get();
            if (threadFancyGraphics == null) {
                threadFancyGraphics = ft$initialFancyGraphics;
                ft$threadedFancyGraphics.set(threadFancyGraphics);
            }
            return threadFancyGraphics;
        }
    }
}
