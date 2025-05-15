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
package com.falsepattern.falsetweaks.modules.occlusion;

import lombok.Getter;
import lombok.val;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL44;

import net.minecraft.client.renderer.GLAllocation;

import java.nio.IntBuffer;
import java.util.Arrays;

public class OcclusionQueryManager implements AutoCloseable {
    private static final int DEFAULT_POOL_SIZE = 1024;
    @Getter
    private final int queryPoolSize;
    private final IntBuffer occlusionResult = GLAllocation.createDirectIntBuffer(1);
    private final IntBuffer occlusionQueries;
    private final QueryFinishCallback[] finishCallbacks;
    private final QueryLauncher launcher = new QueryLauncher();

    private int nextQueryIndex = 0;
    private int minActive = 0;
    private int maxActive = 0;


    public OcclusionQueryManager() {
        this(DEFAULT_POOL_SIZE);
    }

    public OcclusionQueryManager(int queryPoolSize) {
        this.queryPoolSize = queryPoolSize;
        occlusionQueries = BufferUtils.createIntBuffer(queryPoolSize);
        GL15.glGenQueries(occlusionQueries);
        finishCallbacks = new QueryFinishCallback[queryPoolSize];
    }

    private OcclusionQueryManager(OcclusionQueryManager old, int newQueries) {
        val oldQueryCount = old.occlusionQueries.remaining();
        queryPoolSize = oldQueryCount + newQueries;
        nextQueryIndex = oldQueryCount;
        occlusionQueries = BufferUtils.createIntBuffer(queryPoolSize);
        occlusionQueries.put(old.occlusionQueries);
        GL15.glGenQueries(occlusionQueries);
        occlusionQueries.position(0);
        finishCallbacks = Arrays.copyOf(old.finishCallbacks, queryPoolSize);
    }

    public OcclusionQueryManager grow(int bySize) {
        return new OcclusionQueryManager(this, bySize);
    }

    public boolean dispatchNewOcclusionQuery(QueryStartCallback queryExecutor) {
        int c = 0;
        int queryIndex = -1;
        while (queryIndex == -1 && c < queryPoolSize) {
            if (finishCallbacks[nextQueryIndex] == null) {
                queryIndex = nextQueryIndex;
            }
            nextQueryIndex = (nextQueryIndex + 1) % queryPoolSize;
            c++;
        }
        if (queryIndex == -1) {

            return false;
        }

        launcher.query = occlusionQueries.get(queryIndex);
        launcher.ready = true;
        val finish = queryExecutor.run(launcher);
        if (launcher.ready) // This should never happen
            throw new AssertionError("Query starter did not create occlusion query");
        if (launcher.pending) // This should never happen
            throw new AssertionError("Query starter did not end occlusion query");

        finishCallbacks[queryIndex] = finish;
        minActive = Math.min(minActive, queryIndex);
        maxActive = Math.max(maxActive, queryIndex);
        return true;
    }

    public void processDispatchedQueries() {
        int remainingMin = 0;
        int remainingMax = 0;
        for (int i = minActive; i <= maxActive; i++) {
            val finish = finishCallbacks[i];
            if (finish == null) {
                continue;
            }
            if (finish.earlyDiscard()) {
                finishCallbacks[i] = null;
                continue;
            }
            if (finish.preCheck()) {
                val query = occlusionQueries.get(i);
                occlusionResult.put(0, -1);
                GL15.glGetQueryObjectu(query, GL44.GL_QUERY_RESULT_NO_WAIT, occlusionResult);
                val res = occlusionResult.get(0);
                if (res != -1) {
                    finish.run(res);
                    finishCallbacks[i] = null;
                    continue;
                }
            }
            if (remainingMin == 0)
                remainingMin = i;
            remainingMax = i;
        }
        minActive = remainingMin;
        maxActive = remainingMax;
    }

    public static class QueryLauncher {
        private int query = 0;
        private boolean pending = false;
        private boolean ready = true;
        public void begin(int target) {
            if (!ready) //this should never happen
                throw new AssertionError("Tried to start multiple occlusion queries in a single dispatch!");
            if (pending) //this should never happen
                throw new AssertionError("Tried to start occlusion query twice!");
            GL15.glBeginQuery(target, query);
            pending = true;
        }

        public void end(int target) {
            GL15.glEndQuery(target);
            pending = false;
            ready = false;
        }
    }

    public interface QueryStartCallback {
        QueryFinishCallback run(QueryLauncher launcher);
    }

    public interface QueryFinishCallback {
        boolean earlyDiscard();
        boolean preCheck();
        void run(int queryResult);
    }


    @Override
    public void close() {
        GL15.glDeleteQueries(occlusionQueries);
    }
}
