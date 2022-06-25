package com.falsepattern.triangulator.mixin.helper;

import com.falsepattern.triangulator.TriConfig;
import com.falsepattern.triangulator.Triangulator;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LeakFix {
    public static final LeakFix INSTANCE = new LeakFix();
    public static final boolean ENABLED;

    @Getter
    private static int activeBufferCount = 0;
    private static boolean debugText = false;
    private static TIntList cachedBuffers = new TIntArrayList();
    private static int allocs = 0;
    private static int totalAllocs = 0;
    private static int hits = 0;
    private static int misses = 0;

    private static long lastGC = 0;

    public static int getCachedBufferCount() {
        return cachedBuffers.size();
    }

    public static void gc() {
        allocs = 0;
        int currentSize = getCachedBufferCount();
        int targetSize = TriConfig.MEMORY_LEAK_FIX_CACHE_SIZE_TARGET;
        int allocationCount = targetSize - currentSize;
        if (allocationCount > 0) {
            int base = GL11.glGenLists(allocationCount * 3);
            for (int i = 0; i < allocationCount; i++) {
                cachedBuffers.add(base + i * 3);
            }
        } else if (allocationCount < 0) {
            for (int i = currentSize - 1; i >= targetSize; i--) {
                GL11.glDeleteLists(cachedBuffers.removeAt(i), 3);
            }
        }
    }

    public static int allocateWorldRendererBuffer() {
        activeBufferCount++;
        allocs++;
        totalAllocs++;
        int size = getCachedBufferCount();
        if (size == 0) {
            misses++;
            return GL11.glGenLists(3);
        } else {
            hits++;
            return cachedBuffers.removeAt(size - 1);
        }
    }

    public static void releaseWorldRendererBuffer(int buffer) {
        activeBufferCount--;
        cachedBuffers.add(buffer);
    }


    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Pre e) {
        if (LeakFix.ENABLED) {
            if (e.type.equals(RenderGameOverlayEvent.ElementType.DEBUG)) {
                debugText = true;
                return;
            }
            if (!debugText || !(e instanceof RenderGameOverlayEvent.Text) || !e.type.equals(RenderGameOverlayEvent.ElementType.TEXT)) return;
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
                txt.right.add("Total allocation cache hits: " + hits + " (" + (int)((100f / totalAllocs) * hits) + "%)");
                txt.right.add("Total allocation cache misses: " + misses + " (" + (int)((100f / totalAllocs) * misses) + "%)");
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorldLastEvent(RenderWorldLastEvent e) {
        long time = System.nanoTime();
        float secondsSinceLastGC = (time - lastGC) / 1000000000f;
        int cacheSize = getCachedBufferCount();
        if (secondsSinceLastGC > 5 ||
            (secondsSinceLastGC > 1 && (cacheSize < (TriConfig.MEMORY_LEAK_FIX_CACHE_SIZE_TARGET / 2) || cacheSize > (TriConfig.MEMORY_LEAK_FIX_CACHE_SIZE_TARGET * 2)))) {
            gc();
            lastGC = time;
        }
    }

    static {
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
