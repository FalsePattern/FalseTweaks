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

package com.falsepattern.falsetweaks.api.triangulator;

import com.falsepattern.lib.StableAPI;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@StableAPI(since = "2.0.0")
public interface ToggleableTessellator {
    /**
     * Temporarily disables triangulation mode <em>when already rendering</em>. This is useful for rendering triangle-based meshes
     * into the chunk, which is quad based.
     */
    @StableAPI.Expose
    void suspendQuadTriangulation();

    /**
     * Disables the effect of {@link #suspendQuadTriangulation()}.
     */
    @StableAPI.Expose
    void resumeQuadTriangulation();

    @StableAPI.Expose
    boolean isQuadTriangulationSuspended();

    /**
     * Completely disables triangulation and falls back to quad rendering.
     * Should not be called after mod loading has finished.
     */
    @StableAPI.Expose
    void disableTriangulator();

    /**
     * Disables the effect of {@link #disableTriangulator()}. If it was called multiple times, this method also needs to
     * be called at least the same amount of times to re-enable it.
     * Should not be called after mod loading has finished.
     */
    @StableAPI.Expose
    void enableTriangulator();

    /**
     * Same as {@link #disableTriangulator()}, but only applies to the current tessellator object.
     */
    @StableAPI.Expose
    void disableTriangulatorLocal();

    /**
     * Same as {@link #enableTriangulator()} ()}, but only applies to the current tessellator object.
     */
    @StableAPI.Expose
    void enableTriangulatorLocal();

    /**
     * Utility for keeping track of current render pass.
     */
    @StableAPI.Expose
    void pass(int pass);

    /**
     * Utility for keeping track of current render pass.
     */
    @StableAPI.Expose
    int pass();

    @StableAPI.Expose
    boolean isTriangulatorDisabled();

    @StableAPI.Expose(since = "2.4.0")
    boolean drawingTris();
}
