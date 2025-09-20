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

package com.falsepattern.falsetweaks.modules.misc;

import com.falsepattern.falsetweaks.Share;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.val;

import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.NextTickListEntry;


public class ChunkPendingBlockUpdateMap {

    public static final boolean DEBUG = Boolean.parseBoolean(System.getProperty(
            "coretweaks.optimizeGetPendingBlockUpdates.debug",
            "false"));
    public static final boolean LENIENT_MODE = Boolean.parseBoolean(System.getProperty(
            "coretweaks.optimizeGetPendingBlockUpdates.lenient",
            "false"));

    public static void add(IPendingBlockUpdatesWorldServer ws, NextTickListEntry e) {
        val map = ws.ft$getChunkPendingUpdatesMap();
        val key = ChunkCoordIntPair.chunkXZ2Int(e.xCoord >> 4, e.zCoord >> 4);

        val chunkSet = map.computeIfAbsent(key, k -> new ObjectRBTreeSet<>());
        chunkSet.add(e);
    }

    public static void remove(IPendingBlockUpdatesWorldServer ws, NextTickListEntry e) {
        val map = ws.ft$getChunkPendingUpdatesMap();
        val key = ChunkCoordIntPair.chunkXZ2Int(e.xCoord >> 4, e.zCoord >> 4);

        val chunkSet = map.get(key);
        if (chunkSet != null) {
            chunkSet.remove(e);
            if (chunkSet.isEmpty()) {
                map.remove(key);
            }
        }
    }

    public static ObjectSet<NextTickListEntry> get(IPendingBlockUpdatesWorldServer ws, int cx, int cz) {
        val map = ws.ft$getChunkPendingUpdatesMap();
        val key = ChunkCoordIntPair.chunkXZ2Int(cx, cz);

        return map.get(key);
    }

    public static void removeKey(IPendingBlockUpdatesWorldServer ws, int cx, int cz) {
        val map = ws.ft$getChunkPendingUpdatesMap();
        long key = ChunkCoordIntPair.chunkXZ2Int(cx, cz);

        map.remove(key);
    }

    public static boolean isEmpty(IPendingBlockUpdatesWorldServer ws) {
        val map = ws.ft$getChunkPendingUpdatesMap();

        return map == null || map.isEmpty();
    }

    public static void onTick(IPendingBlockUpdatesWorldServer ws) {
        if (DEBUG) {
            val map = ws.ft$getChunkPendingUpdatesMap();
            val debug = new StringBuilder();
            for (val e : Long2ObjectMaps.fastIterable(map)) {
                val k = e.getLongKey();
                val cx = chunkCoordPairToX(k);
                val cz = chunkCoordPairToZ(k);
                debug.append("(")
                     .append(cx)
                     .append(", ")
                     .append(cz)
                     .append("), ");
            }
            if (!map.isEmpty()) {
                Share.log.info("{}: {}", map.size(), debug);
            }
        }
    }

    private static int chunkCoordPairToX(long pair) {
        return (int) (pair & 4294967295L);
    }

    private static int chunkCoordPairToZ(long pair) {
        return (int) ((pair >> 32) & 4294967295L);
    }

    public static void onError() {
        System.out.println("ERROR");
    }

}