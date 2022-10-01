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
    private static boolean managedMode = false;

    public static void setManagedMode(boolean managedMode) {
        currentItemLayer = 0;
        Data.managedMode = managedMode;
        lastUsedSprite = null;
    }

    public static boolean isManagedMode() {
        return managedMode;
    }

    public static void incrementCurrentItemLayer() {
        currentItemLayer += 1;
    }
}
