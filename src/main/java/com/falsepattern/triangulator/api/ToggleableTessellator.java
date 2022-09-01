/*
 * Triangulator
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
     */
    void disableTriangulator();

    /**
     * Disables the effect of {@link #disableTriangulator()}. If it was called multiple times, this method also needs to
     * be called at least the same amount of times to re-enable it.
     */
    void enableTriangulator();

    boolean isTriangulatorDisabled();
}
