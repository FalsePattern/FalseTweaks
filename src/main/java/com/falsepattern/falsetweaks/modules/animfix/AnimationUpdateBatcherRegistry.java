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

package com.falsepattern.falsetweaks.modules.animfix;

import com.falsepattern.falsetweaks.api.animfix.IAnimationUpdateBatcher;
import com.falsepattern.falsetweaks.api.animfix.IAnimationUpdateBatcherFactory;
import lombok.val;

import net.minecraft.client.renderer.texture.TextureMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimationUpdateBatcherRegistry {
    private static final List<Integer> priorities = new ArrayList<>();
    private static final Map<Integer, IAnimationUpdateBatcherFactory> factories = new HashMap<>();
    public static TextureMap currentAtlas = null;
    public static IAnimationUpdateBatcher batcher = null;
    public static String currentName = null;

    static {
        registerBatcherFactory(DefaultAnimationUpdateBatcher::new, 0);
    }

    public static void registerBatcherFactory(IAnimationUpdateBatcherFactory factory, int priority) {
        while (priorities.contains(priority)) {
            priority++;
        }
        priorities.add(priority);
        factories.put(priority, factory);
        priorities.sort(Comparator.naturalOrder());
    }

    public static IAnimationUpdateBatcher newBatcher(int xOffset, int yOffset, int width, int height, int mipLevel) {
        for (val priority : priorities) {
            val factory = factories.get(priority);
            val batcher = factory.createBatcher(xOffset, yOffset, width, height, mipLevel);
            if (batcher != null) {
                return batcher;
            }
        }
        throw new IllegalStateException("Could not construct animation update batcher");
    }
}
