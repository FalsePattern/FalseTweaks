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

package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates;

import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.World;

import java.util.concurrent.atomic.AtomicLong;

/**
 * LongHashMap is NOT thread-safe when reading+writing at the same time, so we need to mutex it.
 * <p>
 * However, I don't want to add any extra unneeded latency to the client thread, so we always busy-wait to minimize
 * the wait time.
 */
@Mixin(ChunkProviderClient.class)
public abstract class ChunkProviderClientMixin {
    @Unique
    private volatile AtomicLong ft$writeCount;

    @Inject(method = "<init>",
            at = @At("RETURN"),
            require = 1)
    private void initMutex(World p_i1184_1_, CallbackInfo ci) {
        ft$writeCount = new AtomicLong();
    }

    @Redirect(method = "unloadChunk",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/util/LongHashMap;remove(J)Ljava/lang/Object;"),
              require = 1)
    private Object threadSafeUnload(LongHashMap instance, long id) {
        ft$writeCount.incrementAndGet();
        Object result = instance.remove(id);
        ft$writeCount.incrementAndGet();
        return result;
    }

    @Redirect(method = "loadChunk",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/util/LongHashMap;add(JLjava/lang/Object;)V"),
              require = 1)
    private void threadSafeLoad(LongHashMap instance, long id, Object value) {
        ft$writeCount.incrementAndGet();
        instance.add(id, value);
        ft$writeCount.incrementAndGet();
    }

    @Redirect(method = "provideChunk",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/util/LongHashMap;getValueByKey(J)Ljava/lang/Object;"),
              require = 1)
    private Object threadSafeGet(LongHashMap instance, long id) {
        Object result = null;
        long expectedWriteCount;
        boolean retry;
        do {
            retry = false;
            do {
                expectedWriteCount = ft$writeCount.get();
            } while (expectedWriteCount % 2 != 0);
            try {
                result = instance.getValueByKey(id);
            } catch (ArrayIndexOutOfBoundsException ignored) {
                retry = true;
            }
        } while (retry || !ft$writeCount.compareAndSet(expectedWriteCount, expectedWriteCount));
        return result;
    }
}
