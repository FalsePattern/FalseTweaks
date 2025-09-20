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

package com.falsepattern.falsetweaks.modules.threadedupdates.saftey;

import com.falsepattern.falsetweaks.api.threading.ThreadSafeBlockRenderer;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
import lombok.val;

import net.minecraft.block.Block;
import cpw.mods.fml.client.registry.RenderingRegistry;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ThreadedBlockSafetyRegistry {
    private static final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();
    private static final Reference2BooleanMap<Block> ISBRH_SAFETY_MAP = new Reference2BooleanOpenHashMap<>();

    public static boolean canBlockRenderOffThread(Block block) {
        Lock lock = LOCK.readLock();
        while (!lock.tryLock()) {
            Thread.yield();
        }
        try {
            if (ISBRH_SAFETY_MAP.containsKey(block)) {
                return ISBRH_SAFETY_MAP.getBoolean(block);
            }

            //upgrade to write lock
            //Releasing the lock here to prevent a deadlock scenario, then hitting the map again
            lock.unlock();
            lock = LOCK.writeLock();
            while (!lock.tryLock()) {
                Thread.yield();
            }
            return ISBRH_SAFETY_MAP.computeIfAbsent(block, ThreadedBlockSafetyRegistry::getCanRenderOffThread);

        } finally {
            lock.unlock();
        }
    }

    private static boolean getCanRenderOffThread(Block block) {
        val renderer = ((IRenderingRegistryExt) RenderingRegistry.instance()).getISBRH(block);
        return renderer instanceof ThreadSafeBlockRenderer;
    }
}
