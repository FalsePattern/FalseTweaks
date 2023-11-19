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

package com.falsepattern.falsetweaks.modules.voxelizer;

import lombok.Getter;
import lombok.Setter;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class Data {
    public static boolean enchantmentGlintTextureBound = false;
    @Getter
    private static int currentItemLayer = 0;
    @Getter
    @Setter
    private static TextureAtlasSprite lastUsedSprite = null;
    private static int managedMode = 0;

    public static boolean isManagedMode() {
        return managedMode != 0;
    }

    public static void setManagedMode(boolean managedMode) {
        if (managedMode) {
            Data.managedMode++;
        } else {
            Data.managedMode--;
        }
        if (Data.managedMode < 0) {
            Data.managedMode = 0;
        }
        if (Data.managedMode == 1 && managedMode || Data.managedMode == 0 && !managedMode) {
            currentItemLayer = 0;
        }
        lastUsedSprite = null;
    }

    public static void incrementCurrentItemLayer() {
        currentItemLayer += 1;
    }
}
