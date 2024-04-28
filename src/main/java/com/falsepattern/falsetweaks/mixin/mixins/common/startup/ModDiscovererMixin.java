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

package com.falsepattern.falsetweaks.mixin.mixins.common.startup;

import com.falsepattern.falsetweaks.modules.startup.RegexHelper;
import com.falsepattern.falsetweaks.modules.startup.ThreadSafeASMDataTable;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.val;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.LoaderException;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.discovery.ASMDataTable;
import cpw.mods.fml.common.discovery.ModCandidate;
import cpw.mods.fml.common.discovery.ModDiscoverer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(value = ModDiscoverer.class,
       remap = false)
public abstract class ModDiscovererMixin {
    @Shadow
    private List<ModCandidate> candidates;
    @Shadow
    private ASMDataTable dataTable;
    @Shadow
    private List<File> nonModLibs;
    private String fileName;

    @Redirect(method = "findModDirMods(Ljava/io/File;[Ljava/io/File;)V",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/regex/Pattern;matcher(Ljava/lang/CharSequence;)Ljava/util/regex/Matcher;"),
              require = 1)
    private Matcher noMatcher(Pattern instance, CharSequence charSequence) {
        fileName = charSequence.toString();
        return null;
    }

    @Redirect(method = "findModDirMods(Ljava/io/File;[Ljava/io/File;)V",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/regex/Matcher;matches()Z"),
              require = 1)
    private boolean fastMatch(Matcher instance) {
        return RegexHelper.zipJarRegex(fileName);
    }

    @Redirect(method = "findModDirMods(Ljava/io/File;[Ljava/io/File;)V",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/regex/Matcher;group(I)Ljava/lang/String;"),
              require = 1)
    private String noGroup(Matcher instance, int i) {
        return fileName;
    }


    /**
     * @author _
     * @reason _
     */
    @Overwrite
    public List<ModContainer> identifyMods() {
        val executor = Executors.newWorkStealingPool();
        List<ModContainer> modList = Lists.newArrayList();
        int size = candidates.size();
        val futures = new ArrayList<Future<List<ModContainer>>>(size);


        ((ThreadSafeASMDataTable) dataTable).enableMutex(true);
        for (int i = 0; i < size; i++) {
            val candidate = candidates.get(i);
            futures.add(executor.submit(() -> candidate.explore(dataTable)));
        }

        executor.shutdown();

        for (int i = 0; i < size; i++) {
            val candidate = candidates.get(i);
            try {
                List<ModContainer> mods;
                try {
                    mods = futures.get(i).get();
                } catch (ExecutionException e) {
                    throw e.getCause();
                }
                if (mods.isEmpty() && !candidate.isClasspath()) {
                    nonModLibs.add(candidate.getModContainer());
                } else {
                    modList.addAll(mods);
                }
            } catch (LoaderException le) {
                FMLLog.log(Level.WARN, le, "Identified a problem with the mod candidate %s, ignoring this source", candidate.getModContainer());
            } catch (Throwable t) {
                Throwables.propagate(t);
            }
        }

        ((ThreadSafeASMDataTable) dataTable).enableMutex(false);
        return modList;
    }
}
