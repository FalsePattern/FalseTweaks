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

package com.falsepattern.falsetweaks.asm;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;

import java.lang.reflect.Field;
import java.util.AbstractList;
import java.util.List;

public class ASMFixerUtility {
    @SneakyThrows
    public static void removeGTNHLibThreadingHook(Field f) {
        val transformers = (List<IClassTransformer>)f.get(Launch.classLoader);
        transformers.removeIf(ASMFixerUtility::isForbidden);
        f.set(Launch.classLoader, new ThreadSafeTransformerFilterList<>(transformers));
    }

    private static boolean isForbidden(Object obj) {
        val n = obj.getClass().getName();
        return n.startsWith("com.gtnewhorizon.gtnhlib") && n.endsWith("TessellatorRedirectorTransformer");
    }

    @RequiredArgsConstructor
    private static class ThreadSafeTransformerFilterList<T> extends AbstractList<T> {
        private final List<T> real;
        @Override
        public T get(int index) {
            return real.get(index);
        }

        @Override
        public int size() {
            return real.size();
        }

        @Override
        public boolean add(T t) {
            if (isForbidden(t))
                return false;
            return real.add(t);
        }

        @Override
        public T set(int index, T element) {
            if (isForbidden(element))
                return null;
            return real.set(index, element);
        }

        @Override
        public void add(int index, T element) {
            if (isForbidden(element))
                return;
            real.add(index, element);
        }

        @Override
        public T remove(int index) {
            return real.remove(index);
        }
    }
}
