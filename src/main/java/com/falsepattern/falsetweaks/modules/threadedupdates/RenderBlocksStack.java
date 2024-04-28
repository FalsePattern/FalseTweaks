/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2024 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
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

package com.falsepattern.falsetweaks.modules.threadedupdates;

/**
 * A helper class to detect re-entrance of renderBlockByRenderType.
 */
public class RenderBlocksStack {

    private int level;

    /**
     * Re-entrance should be impossible on the render threads, since only vanilla blocks are rendered there. So let's
     * just assume this is the case.
     */
    private boolean isMainThread() {
        return ThreadedChunkUpdateHelper.isMainThread();
    }

    public void push() {
        if (!isMainThread()) {
            return;
        }
        level++;
    }

    public void pop() {
        if (!isMainThread()) {
            return;
        }
        level--;
    }

    public void reset() {
        if (!isMainThread()) {
            return;
        }
        level = 0;
    }

    public int getLevel() {
        if (!isMainThread()) {
            return 0;
        }
        return level;
    }
}
