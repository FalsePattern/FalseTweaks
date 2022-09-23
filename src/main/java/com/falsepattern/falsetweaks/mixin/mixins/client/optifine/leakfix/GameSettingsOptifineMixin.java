/*
 * FalseTweaks
 *
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice, this permission notice and the word "SNEED"
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.falsetweaks.mixin.mixins.client.optifine.leakfix;

import com.falsepattern.falsetweaks.config.FTConfig;
import com.falsepattern.falsetweaks.leakfix.LeakFixState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.settings.GameSettings;

@SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
@Mixin(GameSettings.class)
public abstract class GameSettingsOptifineMixin {
    @Shadow(remap = false)
    public int ofChunkLoading;

    @Inject(method = "updateChunkLoading",
            at = @At(value = "HEAD"),
            remap = false,
            require = 1)
    private void blockMultiThreadedChunkLoading(CallbackInfo ci) {
        if (FTConfig.MEMORY_LEAK_FIX != LeakFixState.Disable) {
            ofChunkLoading = 0;
        }
    }
}
