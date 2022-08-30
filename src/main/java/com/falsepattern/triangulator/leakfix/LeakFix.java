package com.falsepattern.triangulator.leakfix;

import com.falsepattern.triangulator.Triangulator;
import com.falsepattern.triangulator.config.TriConfig;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LeakFix {
    public static final boolean ENABLED;
    private static final LeakFix INSTANCE = new LeakFix();
    @Getter
    private static int activeBufferCount = 0;
    private static boolean debugText = false;
    private static TIntList freshAllocations = new TIntArrayList();
    private static TIntList reusableAllocations = new TIntArrayList();
    private static int allocs = 0;
    private static int totalAllocs = 0;
    private static int hits = 0;
    private static int misses = 0;

    private static long lastGC = 0;

    static {
        if(TriConfig.MEMORY_LEAK_FIX == null) {
            Triangulator.triLog.info("Disabling leak fix because of uninitialized config.");
            ENABLED = false;
        } else {
            switch (TriConfig.MEMORY_LEAK_FIX) {
                default:
                    Triangulator.triLog.info("Disabling leak fix because of config flag.");
                    ENABLED = false;
                    break;
                case Auto:
                    boolean isAMD = GL11.glGetString(GL11.GL_VENDOR).toLowerCase().contains("amd");
                    if (isAMD) {
                        Triangulator.triLog.info("Enabling leak fix because an AMD gpu was detected.");
                        ENABLED = true;
                    } else {
                        Triangulator.triLog.info("Disabling leak fix because an AMD gpu was NOT detected.");
                        ENABLED = false;
                    }
                    break;
                case Enable:
                    Triangulator.triLog.info("Enabling leak fix because of config flag.");
                    ENABLED = true;
                    break;
            }
        }
    }

    public static int getCachedBufferCount() {
        return freshAllocations.size() + reusableAllocations.size();
    }

    public static void registerBus() {
        MinecraftForge.EVENT_BUS.register(INSTANCE);
    }

    public static void activate() {
        activeBufferCount++;
    }

    public static void gc() {
        allocs = 0;
        int reusables = reusableAllocations.size();
        for (int i = 0; i < reusables; i++) {
            GL11.glDeleteLists(reusableAllocations.get(i), 3);
        }
        reusableAllocations.clear();
        int currentSize = freshAllocations.size();
        int targetSize = TriConfig.MEMORY_LEAK_FIX_CACHE_SIZE_TARGET;
        int allocationCount = targetSize - currentSize;
        if (allocationCount > 0) {
            int base = GL11.glGenLists(allocationCount * 3);
            for (int i = 0; i < allocationCount; i++) {
                freshAllocations.add(base + i * 3);
            }
        } else if (allocationCount < 0) {
            for (int i = currentSize - 1; i >= targetSize; i--) {
                GL11.glDeleteLists(freshAllocations.removeAt(i), 3);
            }
        }
    }

    public static int allocateWorldRendererBuffer() {
        activeBufferCount++;
        allocs++;
        totalAllocs++;
        int reusables = reusableAllocations.size();
        if (reusables > 0) {
            hits++;
            return reusableAllocations.removeAt(reusables - 1);
        }
        int fresh = freshAllocations.size();
        if (fresh > 0) {
            hits++;
            return freshAllocations.removeAt(fresh - 1);
        }
        misses++;
        return GL11.glGenLists(3);
    }

    public static void releaseWorldRendererBuffer(int buffer) {
        activeBufferCount--;
        for (int i = 0; i < 3; i++) {
            GL11.glNewList(buffer + i, GL11.GL_COMPILE);
            GL11.glEndList();
        }
        reusableAllocations.add(buffer);
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Pre e) {
        if (LeakFix.ENABLED) {
            if (e.type.equals(RenderGameOverlayEvent.ElementType.DEBUG)) {
                debugText = true;
                return;
            }
            if (!debugText || !(e instanceof RenderGameOverlayEvent.Text) ||
                !e.type.equals(RenderGameOverlayEvent.ElementType.TEXT)) {
                return;
            }
            debugText = false;
            val txt = (RenderGameOverlayEvent.Text) e;
            txt.right.add(null);
            txt.right.add("[Triangulator Leak Fix]");
            int active = LeakFix.getActiveBufferCount();
            int cached = LeakFix.getCachedBufferCount();
            int total = active + cached;
            txt.right.add("Total chunk renderers: " + total);
            txt.right.add("Active chunk renderers: " + active);
            txt.right.add("Cached chunk renderers: " + cached);
            if (Minecraft.getMinecraft().mcProfiler.profilingEnabled) {
                //Verbose info
                txt.right.add("Allocations since last GC cycle: " + allocs);
                txt.right.add("Total allocations: " + totalAllocs);
                txt.right.add(
                        "Total allocation cache hits: " + hits + " (" + (int) ((100f / totalAllocs) * hits) + "%)");
                txt.right.add(
                        "Total allocation cache misses: " + misses + " (" + (int) ((100f / totalAllocs) * misses) +
                        "%)");
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorldLastEvent(RenderWorldLastEvent e) {
        long time = System.nanoTime();
        float secondsSinceLastGC = (time - lastGC) / 1000000000f;
        int cacheSize = getCachedBufferCount();
        if (secondsSinceLastGC > 5 || (secondsSinceLastGC > 1 &&
                                       (cacheSize < (TriConfig.MEMORY_LEAK_FIX_CACHE_SIZE_TARGET / 2) ||
                                        cacheSize > (TriConfig.MEMORY_LEAK_FIX_CACHE_SIZE_TARGET * 2)))) {
            gc();
            lastGC = time;
        }
    }
}
