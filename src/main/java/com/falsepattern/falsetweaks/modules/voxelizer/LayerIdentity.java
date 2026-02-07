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

@RequiredArgsConstructor
public final class LayerIdentity {
    private final @NotNull String iconName;
    private final int frameCounter;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LayerIdentity))
            return false;
        val o = (LayerIdentity) obj;
        return equals(o.iconName, o.frameCounter);
    }

    public boolean equals(@NotNull String iconName, int frameCounter) {
        return frameCounter == this.frameCounter && iconName.equals(this.iconName);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = result * 31 + iconName.hashCode();
        result = result * 31 + Integer.hashCode(frameCounter);
        return result;
    }

    @Override
    public String toString() {
        return iconName + "|" + frameCounter;
    }
}
