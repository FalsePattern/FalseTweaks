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

package com.falsepattern.falsetweaks.asm.modules.threadedupdates.compat;

import com.falsepattern.falsetweaks.asm.modules.threadedupdates.Threading_ThreadSafeBlockRendererInjector;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;

class AngelicaRemappingAdapter extends RemappingClassAdapter {
    AngelicaRemappingAdapter(ClassVisitor cv) {
        super(cv, AngelicaRemapper.INSTANCE);
    }

    private static class AngelicaRemapper extends Remapper {
        public static final AngelicaRemapper INSTANCE = new AngelicaRemapper();
        private static final Object2ObjectMap<String, String> MAPPINGS = new Object2ObjectArrayMap<>(2);

        static {
            MAPPINGS.put("com/gtnewhorizons/angelica/api/ThreadSafeISBRH",
                         Threading_ThreadSafeBlockRendererInjector.THREAD_SAFE_ANNOTATION_InternalName);
            MAPPINGS.put("com/gtnewhorizons/angelica/api/ThreadSafeISBRHFactory",
                         Threading_ThreadSafeBlockRendererInjector.THREAD_SAFE_FACTORY_InternalName);
        }

        @Override
        public String map(String typeName) {
            return MAPPINGS.getOrDefault(typeName, typeName);
        }
    }
}
