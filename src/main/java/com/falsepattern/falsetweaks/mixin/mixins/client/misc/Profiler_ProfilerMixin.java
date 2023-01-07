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

package com.falsepattern.falsetweaks.mixin.mixins.client.misc;

import lombok.val;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.profiler.Profiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Mixin(Profiler.class)
public abstract class Profiler_ProfilerMixin {
    @Shadow
    public boolean profilingEnabled;

    @Shadow
    @Final
    private Map<String, Long> profilingMap;

    private int counter = 0;

    @Inject(method = "clearProfiling",
            at = @At("HEAD"),
            require = 1)
    private void resetCounter(CallbackInfo ci) {
        counter = 0;
    }

    /**
     * @author FalsePattern
     * @reason Allows for more accurate profiling
     */
    @Overwrite
    public List<Profiler.Result> getProfilingData(String current) {
        if (!this.profilingEnabled) {
            return null;
        }
        long rootNanos = this.profilingMap.getOrDefault("root", 0L);
        long currentNanos = this.profilingMap.getOrDefault(current, -1L);
        val results = new ArrayList<Profiler.Result>();

        if (current.length() > 0) {
            current = current + ".";
        }

        long subNanos = 0L;
        for (String name: this.profilingMap.keySet()) {
            if (name.length() > current.length() && name.startsWith(current) &&
                name.indexOf(".", current.length() + 1) < 0) {
                subNanos += this.profilingMap.get(name);
            }
        }

        float f = (float) subNanos;

        if (subNanos < currentNanos) {
            subNanos = currentNanos;
        }

        if (rootNanos < subNanos) {
            rootNanos = subNanos;
        }

        for (String entry: this.profilingMap.keySet()) {
            if (entry.length() > current.length() && entry.startsWith(current) && entry.indexOf(".", current.length() + 1) < 0) {
                long entryNanos = this.profilingMap.get(entry);
                double percent = (double) entryNanos * 100.0D / (double) subNanos;
//                double totalPercent = (double) entryNanos * 100.0D / (double) rootNanos;
                String entryName = entry.substring(current.length());

                if (entryName.length() > 32) {
                    entryName = entryName.substring(0, 32) + "...";
                }
                results.add(new Profiler.Result(entryName, percent, entryNanos / (double) counter));
            }
        }

        this.profilingMap.replaceAll((s, v) -> v * 999L / 1000L);
        counter++;
        if (counter > 1000) {
            counter = 1000;
        }

        if ((float) subNanos > f) {
            results.add(new Profiler.Result("unspecified", ((double) subNanos - f) * 100.0D / (double) subNanos,
                                            ((double) subNanos - f) * 100.0D / (double) rootNanos));
        }

        results.sort(Profiler.Result::compareTo);
        Collections.sort(results);
        results.add(0, new Profiler.Result(current, 100.0D, (double) subNanos * 100.0D / (double) rootNanos));
        return results;
    }
}
