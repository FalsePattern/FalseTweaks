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

package com.falsepattern.falsetweaks.mixin.mixins.common.misc;

import com.falsepattern.falsetweaks.modules.misc.ChunkPendingBlockUpdateMap;
import com.falsepattern.falsetweaks.modules.misc.IPendingBlockUpdatesWorldServer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.val;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static com.falsepattern.falsetweaks.modules.misc.ChunkPendingBlockUpdateMap.DEBUG;
import static com.falsepattern.falsetweaks.modules.misc.ChunkPendingBlockUpdateMap.LENIENT_MODE;

@Mixin(WorldServer.class)
public abstract class PendingBlockUpdates_WorldServerMixin implements IPendingBlockUpdatesWorldServer {

    @Shadow
    @Final
    static private Logger logger;
    @Shadow
    private Set pendingTickListEntriesHashSet;
    /**
     * All work to do in future ticks.
     */
    @Shadow
    private TreeSet pendingTickListEntriesTreeSet;
    @Shadow
    private List pendingTickListEntriesThisTick;
    /**
     * Chunk coordinate -> Block updates in that chunk
     */
    @Unique
    private Long2ObjectMap<ObjectSet<NextTickListEntry>> ft$chunkPendingUpdatesMap;

    @Inject(method = "tick",
            at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ChunkPendingBlockUpdateMap.onTick(this);
    }

    @Redirect(method = {"scheduleBlockUpdateWithPriority", "func_147446_b"},
              at = @At(value = "INVOKE",
                       target = "Ljava/util/TreeSet;add(Ljava/lang/Object;)Z",
                       remap = false))
    public boolean redirectAdd(TreeSet set, Object o) {
        ChunkPendingBlockUpdateMap.add(this, (NextTickListEntry) o);
        return set.add(o);
    }

    @Redirect(method = {"tickUpdates"},
              at = @At(value = "INVOKE",
                       target = "Ljava/util/TreeSet;remove(Ljava/lang/Object;)Z",
                       remap = false))
    public boolean redirectRemove(TreeSet set, Object o) {
        ChunkPendingBlockUpdateMap.remove(this, (NextTickListEntry) o);
        return set.remove(o);
    }

    /**
     * @author makamys
     * @reason Use map instead of iterating over the full contents of pendingTickListEntriesTreeSet for more fastness.
     *
     */
    @Overwrite
    public List<NextTickListEntry> getPendingBlockUpdates(Chunk p_72920_1_, boolean p_72920_2_) {
        ObjectList<NextTickListEntry> arraylist = null;
        val chunkcoordintpair = p_72920_1_.getChunkCoordIntPair();
        int i = (chunkcoordintpair.chunkXPos << 4) - 2;
        int j = i + 16 + 2;
        int k = (chunkcoordintpair.chunkZPos << 4) - 2;
        int l = k + 16 + 2;

        // New code start

        if (!ChunkPendingBlockUpdateMap.isEmpty(this)) {
            for (int cx = p_72920_1_.xPosition - 1; cx <= p_72920_1_.xPosition + 1; cx++) {
                for (int cz = p_72920_1_.zPosition - 1; cz <= p_72920_1_.zPosition + 1; cz++) {
                    val chunkSet = ChunkPendingBlockUpdateMap.get(this, cx, cz);
                    if (chunkSet != null) {
                        Iterator<NextTickListEntry> it = chunkSet.iterator();
                        while (it.hasNext()) {
                            NextTickListEntry nte = it.next();
                            if (nte.xCoord >= i && nte.xCoord < j && nte.zCoord >= k && nte.zCoord < l) {
                                if (p_72920_2_) {
                                    this.pendingTickListEntriesHashSet.remove(nte);
                                    this.pendingTickListEntriesTreeSet.remove(nte);
                                    it.remove();
                                    if (chunkSet.isEmpty()) {
                                        ChunkPendingBlockUpdateMap.removeKey(this, cx, cz);
                                    }
                                }

                                if (arraylist == null) {
                                    arraylist = new ObjectArrayList<>(128);
                                }

                                arraylist.add(nte);
                            }
                        }
                    }
                }
            }
        }

        if (!LENIENT_MODE && arraylist != null) {
            arraylist.sort(null);
        }

        List<NextTickListEntry> debug_myList = null;
        if (DEBUG && arraylist != null) {
            debug_myList = new ArrayList<>(arraylist);
            arraylist.clear();
        }

        for (int i1 = DEBUG ? 0 : 1; i1 < 2; ++i1)
        // New code end
        {
            if (DEBUG && i1 == 1) {
                if (!((debug_myList == null && arraylist == null) || (Objects.equals(debug_myList, arraylist)))) {
                    ChunkPendingBlockUpdateMap.onError();
                }
            }

            Iterator iterator;

            if (i1 == 0) {
                iterator = this.pendingTickListEntriesTreeSet.iterator();
            } else {
                iterator = this.pendingTickListEntriesThisTick.iterator();

                if (!this.pendingTickListEntriesThisTick.isEmpty()) {
                    logger.debug("toBeTicked = " + this.pendingTickListEntriesThisTick.size());
                }
            }

            while (iterator.hasNext()) {
                NextTickListEntry nextticklistentry = (NextTickListEntry) iterator.next();

                if (nextticklistentry.xCoord >= i &&
                    nextticklistentry.xCoord < j &&
                    nextticklistentry.zCoord >= k &&
                    nextticklistentry.zCoord < l) {
                    if (p_72920_2_) {
                        this.pendingTickListEntriesHashSet.remove(nextticklistentry);
                        iterator.remove();
                    }

                    if (arraylist == null) {
                        arraylist = new ObjectArrayList<>(128);
                    }

                    arraylist.add(nextticklistentry);
                }
            }
        }

        return arraylist;
    }

    @Override
    public Long2ObjectMap<ObjectSet<NextTickListEntry>> ft$getChunkPendingUpdatesMap() {
        if (ft$chunkPendingUpdatesMap == null) {
            ft$chunkPendingUpdatesMap = new Long2ObjectOpenHashMap<>();
        }
        return ft$chunkPendingUpdatesMap;
    }
}