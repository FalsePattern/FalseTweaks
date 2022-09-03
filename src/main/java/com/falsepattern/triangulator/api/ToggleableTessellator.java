package com.falsepattern.triangulator.api;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface ToggleableTessellator {
    /**
     * Temporarily disables triangulation mode <em>when already rendering</em>. This is useful for rendering triangle-based meshes
     * into the chunk, which is quad based.
     */
    void suspendQuadTriangulation();

    /**
     * Disables the effect of {@link #suspendQuadTriangulation()}.
     */
    void resumeQuadTriangulation();

    boolean isQuadTriangulationSuspended();

    /**
     * Completely disables triangulation and falls back to quad rendering.
     * Should not be called after mod loading has finished.
     */
    void disableTriangulator();

    /**
     * Disables the effect of {@link #disableTriangulator()}. If it was called multiple times, this method also needs to
     * be called at least the same amount of times to re-enable it.
     * Should not be called after mod loading has finished.
     */
    void enableTriangulator();

    boolean isTriangulatorDisabled();
}
