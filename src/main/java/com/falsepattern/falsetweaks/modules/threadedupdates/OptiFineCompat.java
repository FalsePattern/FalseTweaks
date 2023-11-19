/*
 * FalseTweaks
 *
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice, and this permission notice shall be included in all copies
 * or substantial portions of the Software.
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

package com.falsepattern.falsetweaks.modules.threadedupdates;

import shadersmod.client.Shaders;
import stubpackage.Config;

import cpw.mods.fml.client.FMLClientHandler;

public class OptiFineCompat {
    public static class ThreadSafeEntityData {
        public static final ThreadLocal<ThreadSafeEntityData> TL = ThreadLocal.withInitial(ThreadSafeEntityData::new);

        public final int[] entityData = new int[32];
        public int entityDataIndex = 0;
    }

    public static void popEntity() {
        if (!FMLClientHandler.instance().hasOptifine() || !Config.isShaders()) {
            return;
        }
        Shaders.popEntity();
    }
}
