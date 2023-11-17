package com.falsepattern.falsetweaks.modules.threadedupdates.api;

import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadedChunkUpdateHelper;

import net.minecraft.client.renderer.Tessellator;

public class ThreadedChunkUpdates {

    public static boolean isEnabled() {
        return ThreadedChunkUpdateHelper.instance != null;
    }

    /** Returns the thread-local tessellator instance. Can only be called after init phase. */
    public static Tessellator getThreadTessellator() {
        return ThreadedChunkUpdateHelper.instance.getThreadTessellator();
    }

}
