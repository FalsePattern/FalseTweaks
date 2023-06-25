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

package com.falsepattern.falsetweaks.mprof;

import com.falsepattern.falsetweaks.Share;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Cleanup;
import lombok.val;

import net.minecraft.client.Minecraft;
import net.minecraft.profiler.Profiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MemeProfiler {
    private static final Profiler prof = Minecraft.getMinecraft().mcProfiler;
    private static boolean prevProf = false;
    private static volatile boolean dumping = false;
    private static JsonArray root = null;
    private static JsonArray currentTick = null;
    private static JsonObject currentRenderer = null;
    public static void beginTick() {
        if (!prof.profilingEnabled) {
            if (!prevProf) {
                return;
            }
            val root = MemeProfiler.root;
            MemeProfiler.root = null;
            currentTick = null;
            val dump = new Thread(() -> {
                val format = new SimpleDateFormat("yyyy_MM_dd'T'HH_mm_ss");
                StringBuilder filename = new StringBuilder(format.format(new Date()));
                try {
                    Files.createDirectory(Paths.get("ft_memeprofiling_dumps"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Path dumpFile;
                do {
                    dumpFile = Paths.get("ft_memeprofiling_dumps", filename + ".json");
                    filename.append("_");
                } while (Files.exists(dumpFile));
                try {
                    @Cleanup val stream = Files.newBufferedWriter(dumpFile);
                    val gson = new GsonBuilder().setPrettyPrinting().create();
                    gson.toJson(root, stream);
                } catch (IOException e) {
                    Share.log.warn("Failed to write False Tweaks meme profiling history to file", e);
                }
                dumping = false;
            });
            dump.setName("FTweaks MEMEProfiling Dump Thread");
            dump.start();
            prevProf = false;
            return;
        }
        if (!prevProf) {
            prevProf = true;
            root = new JsonArray();
        }
        currentTick = new JsonArray();
    }

    public static void endTick() {
        if (!prof.profilingEnabled)
            return;
        if (currentTick.size() > 0) {
            root.add(currentTick);
        }
        currentTick = null;
    }

    public static void beginWorldRenderer() {
        if (!prof.profilingEnabled)
            return;
        currentRenderer = new JsonObject();
        currentTick.add(currentRenderer);
        val ns = System.nanoTime();
        currentRenderer.addProperty("nanos", ns);
    }

    public static void endWorldRenderer(int bytesDrawn) {
        if (!prof.profilingEnabled)
            return;
        val ns = System.nanoTime();
        currentRenderer.addProperty("nanos", ns - currentRenderer.get("nanos").getAsLong());
        currentRenderer.addProperty("bytes", bytesDrawn);
        currentRenderer = null;
    }
}
