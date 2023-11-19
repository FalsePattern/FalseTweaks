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

package com.falsepattern.falsetweaks.modules.triangulator;

import com.falsepattern.falsetweaks.Compat;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true,
           chain = false)
public class ToggleableTessellatorManager {
    public static final ToggleableTessellatorManager INSTANCE = new ToggleableTessellatorManager();

    @Getter
    private int forceQuadRendering = 0;

    public void disableTriangulator() {
        forceQuadRendering++;
    }

    public void enableTriangulator() {
        forceQuadRendering--;
        if (forceQuadRendering < 0) {
            forceQuadRendering = 0;
        }
    }

    public boolean isTriangulatorDisabled() {
        return !Compat.enableTriangulation() || forceQuadRendering != 0;
    }
}
