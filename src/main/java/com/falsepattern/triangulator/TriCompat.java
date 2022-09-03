package com.falsepattern.triangulator;

import com.falsepattern.triangulator.config.TriConfig;
import cpw.mods.fml.common.Loader;
import lombok.Getter;
import net.minecraft.client.renderer.Tessellator;
import org.embeddedt.archaicfix.threadedupdates.api.ThreadedChunkUpdates;

public class TriCompat {
    public static void applyCompatibilityTweaks() {
        if (Loader.isModLoaded("archaicfix")) {
            ArchaicFixCompat.init();
        }
    }

    public static boolean enableTriangulation() {
        return TriConfig.ENABLE_QUAD_TRIANGULATION;
    }

    public static Tessellator tessellator() {
        if(ArchaicFixCompat.isThreadedChunkUpdatingEnabled()) {
            return ArchaicFixCompat.threadTessellator();
        }
        return Tessellator.instance;
    }

    private static class ArchaicFixCompat {

        @Getter
        private static boolean isThreadedChunkUpdatingEnabled;

        private static void init() {
            isThreadedChunkUpdatingEnabled = ThreadedChunkUpdates.isEnabled();
        }

        public static Tessellator threadTessellator() {
            return ThreadedChunkUpdates.getThreadTessellator();
        }
    }
}
