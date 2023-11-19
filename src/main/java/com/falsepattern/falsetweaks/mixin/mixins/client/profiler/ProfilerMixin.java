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

package com.falsepattern.falsetweaks.mixin.mixins.client.profiler;

import com.falsepattern.falsetweaks.Share;
import com.falsepattern.falsetweaks.config.ProfilerConfig;
import com.falsepattern.falsetweaks.modules.profiler.ProfilingNode;
import com.google.gson.stream.JsonWriter;
import lombok.Cleanup;
import lombok.val;
import lombok.var;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.profiler.Profiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Mixin(value = Profiler.class,
       priority = Integer.MAX_VALUE)
public abstract class ProfilerMixin {
    @Shadow
    @Final
    private static Logger logger;
    @Shadow
    public boolean profilingEnabled;

    private ProfilingNode origin;
    private ProfilingNode current;
    private List<ProfilingNode> history;
    private int ctr = 0;

    @Inject(method = "<init>",
            at = @At("RETURN"),
            require = 1)
    private void init(CallbackInfo ci) {
        history = new ArrayList<>();
    }


    /**
     * @author FalsePattern
     * @reason Allows for more accurate profiling
     */
    @Overwrite
    public void clearProfiling() {
        if (ProfilerConfig.DUMP_ON_CLOSE) {
            dump();
        } else {
            history.clear();
            ctr = 0;
        }
        origin = current = ProfilingNode.createRoot();
    }

    /**
     * Start section
     *
     * @author FalsePattern
     * @reason Allows for more accurate profiling
     */
    @Overwrite
    public void startSection(String section) {
        if (profilingEnabled) {
            current = current.getOrCreateChild(section);
            current.start();
        } else if (ProfilerConfig.DUMP_ON_CLOSE) {
            dump();
        }
    }

    /**
     * End section
     *
     * @author FalsePattern
     * @reason Allows for more accurate profiling
     */
    @Overwrite
    public void endSection() {
        if (profilingEnabled) {
            if (current.parent == null) {
                logger.warn("Tried to end a section without starting one!");
                return;
            }
            long delta = current.end();
            if (delta > 100_000_000L) {
                logger.warn("Something's taking too long! '" + current.fullName() + "' took approx " +
                            delta / 1_000_000.0D + " ms");
            }

            current = current.parent;
        }
    }

    /**
     * Get profiling data
     *
     * @author FalsePattern
     * @reason Allows for more accurate profiling
     */
    @Overwrite
    public List<Profiler.Result> getProfilingData(String entry) {
        if (!profilingEnabled) {
            return null;
        }
        beginNextSnapshot();
        val route = entry.split("\\.");
        val nodes = history.stream()
                           .map((node) -> node.findChild(route, 0))
                           .filter(Objects::nonNull)
                           .collect(Collectors.toList());
        val rootTime = history.stream().mapToLong((node) -> node.totalTime).sum();
        var nodeTime = nodes.stream().mapToLong((node) -> node.totalTime).sum();
        val childTimes = new HashMap<String, Long>();
        val totalChildTime = new AtomicLong(0L);
        nodes.forEach((node) -> {
            node.childrenMap.forEach((childName, child) -> {
                childTimes.compute(childName, (k_, v) -> v == null ? child.totalTime : v + child.totalTime);
                totalChildTime.addAndGet(child.totalTime);
            });
        });
        val result = new ArrayList<Profiler.Result>();
        val localPercentMultiplier = 100f / nodeTime;
        val globalAverageMultiplier = 1.00f / (float) history.size();
        val unspecTime = nodeTime - totalChildTime.get();
        if (unspecTime > 0) {
            result.add(new Profiler.Result("unspecified", unspecTime * localPercentMultiplier,
                                           unspecTime * globalAverageMultiplier));
        }
        childTimes.forEach((key, value) -> {
            result.add(new Profiler.Result(key, value * localPercentMultiplier, value * globalAverageMultiplier));
        });
        result.sort(null);
        result.add(0, new Profiler.Result(entry, 100.0D, nodeTime * globalAverageMultiplier));
        return result;
    }

    /**
     * @author FalsePattern
     * @reason Allows for more accurate profiling
     */
    @Overwrite
    public String getNameOfLastSection() {
        return current == null ? "[UNKNOWN]" : current.fullName();
    }

    private void dump() {
        if (history.size() > 0) {
            val oldHistory = history;
            history = new ArrayList<>();
            ctr = 0;
            val dump = new Thread(() -> {
                val format = new SimpleDateFormat("yyyy_MM_dd'T'HH_mm_ss");
                StringBuilder filename = new StringBuilder(format.format(new Date()));
                try {
                    Files.createDirectory(Paths.get("ft_profiling_dumps"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Path dumpFile;
                do {
                    dumpFile = Paths.get("ft_profiling_dumps", filename + ".json");
                    filename.append("_");
                } while (Files.exists(dumpFile));
                try {
                    @Cleanup val stream = Files.newBufferedWriter(dumpFile);
                    @Cleanup val json = new JsonWriter(stream);
                    json.setIndent("  ");
                    json.beginArray();
                    for (val node : oldHistory) {
                        node.toJson(json, false, false);
                    }
                    json.endArray();
                } catch (IOException e) {
                    Share.log.warn("Failed to write False Tweaks profiling history to file", e);
                }
            });
            dump.setName("FTweaks Profiling Dump Thread");
            dump.start();
        }
    }

    private void beginNextSnapshot() {
        if (!ProfilerConfig.DUMP_ON_CLOSE && history.size() >= 1000) {
            history.set(ctr, origin);
            ctr = (ctr + 1) % 1000;
        } else {
            history.add(origin);
        }
        current = origin = ProfilingNode.createRoot();
    }
}
