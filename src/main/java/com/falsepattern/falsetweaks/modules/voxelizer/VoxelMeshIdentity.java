/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2025 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalseTweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * FalseTweaks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalseTweaks. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.falsetweaks.modules.voxelizer;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@RequiredArgsConstructor
public final class VoxelMeshIdentity {
    private static final long HC_MARKER = 0xFFFFFFFF00000000L;
    private final boolean remapUv;
    private final int overlayLayer;
    private final @NotNull LayerIdentity  @NotNull[] layers;
    private long __hashcode = HC_MARKER;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof VoxelMeshIdentity))
            return false;
        val o = (VoxelMeshIdentity) obj;
        return equals(o.remapUv, o.overlayLayer, o.layers);
    }

    public boolean partialEquals(int overlayLayer, boolean remapUv) {
        return remapUv == this.remapUv && overlayLayer == this.overlayLayer;
    }

    public boolean partialEquals(@NotNull LayerIdentity @NotNull[] layers) {
        return Arrays.equals(layers, this.layers);
    }

    public boolean equals(boolean remapUv, int overlayLayer, @NotNull LayerIdentity @NotNull[] layers) {
        return remapUv == this.remapUv && overlayLayer == this.overlayLayer && Arrays.equals(layers, this.layers);
    }

    @Override
    public int hashCode() {
        val hc = __hashcode;
        if (hc != HC_MARKER) {
            return (int)(hc & 0xFFFFFFFFL);
        }
        int result = 1;
        result = 31 * result + Boolean.hashCode(remapUv);
        result = 31 * result + Integer.hashCode(overlayLayer);
        result = 31 * result + Arrays.hashCode(layers);
        __hashcode = result & 0xFFFFFFFFL;
        return result;
    }

    @Override
    public String toString() {
        val result = new StringBuilder();
        if (remapUv) {
            result.append("remap_uv!");
        }
        if (overlayLayer > 0) {
            result.append("overlay")
                  .append(overlayLayer)
                  .append("!");
        }
        for (val layer : layers) {
            result.append(layer)
                  .append('&');
        }
        return result.toString();
    }
}
