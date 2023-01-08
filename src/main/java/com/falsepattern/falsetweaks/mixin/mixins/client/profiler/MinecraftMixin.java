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

package com.falsepattern.falsetweaks.mixin.mixins.client.profiler;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.profiler.Profiler;

import java.text.DecimalFormat;
import java.util.List;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow @Final public Profiler mcProfiler;

    @Inject(method = "runTick",
            at = @At(value = "FIELD",
                     target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/GuiScreen;",
                     ordinal = 0),
            require = 1)
    private void beginScreen(CallbackInfo ci) {
        mcProfiler.endStartSection("screen");
    }

    @ModifyConstant(method = "displayDebugInfo",
                    constant = {@Constant(stringValue = "%",
                                          ordinal = 0),
                                @Constant(stringValue = "%",
                                          ordinal = 2)},
                    require = 1)
    private String noPercent(String og) {
        return "";
    }

    @Redirect(method = "displayDebugInfo",
              at = @At(value = "INVOKE",
                       target = "Ljava/text/DecimalFormat;format(D)Ljava/lang/String;",
                       ordinal = 0),
              require = 1)
    private String modifyHeaderPercentage(DecimalFormat instance, double time) {
        return modifyPercentage(instance, time);
    }

    @Redirect(method = "displayDebugInfo",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/List;size()I",
                       ordinal = 1),
              require = 1)
    private int clampTextListSize(List instance) {
        return Math.min(instance.size(), 20);
    }

    @Redirect(method = "displayDebugInfo",
              at = @At(value = "INVOKE",
                       target = "Ljava/text/DecimalFormat;format(D)Ljava/lang/String;",
                       ordinal = 2),
              require = 1)
    private String modifyEntryPercentage(DecimalFormat instance, double time) {
        return modifyPercentage(instance, time);
    }

    private String modifyPercentage(DecimalFormat instance, double time) {
        String suffix;
        if (time < 100) {
            suffix = "ns";
        } else if (time < 100_000) {
            suffix = "us";
            time /= 1_000;
        }else if (time < 100_000_000) {
            suffix = "ms";
            time /= 1_000_000;
        } else {
            suffix = "s ";
            time /= 1_000_000_000;
        }

        return instance.format(time) + suffix;
    }
}
