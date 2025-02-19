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

package com.falsepattern.falsetweaks.modules.threadedupdates.interop;

import cofh.core.block.BlockCoFHBase;
import cofh.thermalexpansion.block.simple.BlockFrame;
import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadedChunkUpdateHelper;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class ThermalExpansionCompat {
    private static final ThreadLocal<Integer> cofhBlockRenderPass = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<Integer> frameBlockRenderPass = ThreadLocal.withInitial(() -> 0);

    public static void setCofhBlockRenderPass(int pass) {
        if (ThreadedChunkUpdateHelper.isMainThread()) {
            BlockCoFHBase.renderPass = pass;
        } else {
            cofhBlockRenderPass.set(pass);
        }
    }

    public static int getCofhBlockRenderPass() {
        if (ThreadedChunkUpdateHelper.isMainThread()) {
            return BlockCoFHBase.renderPass;
        } else {
            return cofhBlockRenderPass.get();
        }
    }

    public static void setFrameBlockRenderPass(int pass) {
        if (ThreadedChunkUpdateHelper.isMainThread()) {
            BlockFrame.renderPass = pass;
        } else {
            frameBlockRenderPass.set(pass);
        }
    }

    public static int getFrameBlockRenderPass() {
        if (ThreadedChunkUpdateHelper.isMainThread()) {
            return BlockFrame.renderPass;
        } else {
            return frameBlockRenderPass.get();
        }
    }
}
