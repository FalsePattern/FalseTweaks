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

package com.falsepattern.falsetweaks.modules.animfix.stitching;

import com.falsepattern.falsetweaks.modules.animfix.stitching.packing2d.Algorithm;
import com.falsepattern.falsetweaks.modules.animfix.stitching.packing2d.Packer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import net.minecraft.client.renderer.texture.Stitcher;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class TurboStitcher extends SpriteSlot {
    private final int maxWidth;
    private final int maxHeight;
    private final boolean forcePowerOf2;
    private List<SpriteSlot> slots = new ArrayList<>();
    @Getter
    private StitcherState state = StitcherState.SETUP;

    private static int nextPowerOfTwo(int number) {
        number--;
        number |= number >>> 1;
        number |= number >>> 2;
        number |= number >>> 4;
        number |= number >>> 8;
        number |= number >>> 16;
        number++;
        return number;
    }

    public void addSprite(Stitcher.Holder holder) {
        addSprite(new HolderSlot(holder));
    }

    public void addSprite(SpriteSlot rect) {
        verifyState(StitcherState.SETUP);
        slots.add(rect);
    }

    public void reset() {
        slots = new ArrayList<>();
        state = StitcherState.SETUP;
    }

    public void stitch() throws TooBigException {
        verifyState(StitcherState.SETUP);
        width = 0;
        height = 0;
        if (slots.size() == 0) {
            state = StitcherState.STITCHED;
            return;
        }
        for (val slot : slots) {
            width = Math.max(width, slot.width);
        }
        if (forcePowerOf2) {
            width = nextPowerOfTwo(width);
        }
        if (width > maxWidth) {
            throw new TooBigException();
        }
        width = Math.max(width >>> 1, 1);
        List<SpriteSlot> packedSlots;
        do {
            if (width == maxWidth) {
                throw new TooBigException();
            }
            if (forcePowerOf2) {
                width *= 2;
            } else {
                width += Math.min(width, 16);
            }
            if (width > maxWidth) {
                width = maxWidth;
            }
            packedSlots = Packer.pack(slots, Algorithm.FIRST_FIT_DECREASING_HEIGHT, width);
            height = 0;
            for (val sprite : packedSlots) {
                height = Math.max(height, sprite.y + sprite.height);
            }
            if (forcePowerOf2) {
                height = nextPowerOfTwo(height);
            }
        } while (height > maxHeight || height > width);
        slots = packedSlots;
        state = StitcherState.STITCHED;
    }

    public List<Stitcher.Slot> getSlots() {
        return getSlots(new Rect2D());
    }

    public List<Stitcher.Slot> getSlots(Rect2D parent) {
        verifyState(StitcherState.STITCHED);
        val mineSlots = new ArrayList<Stitcher.Slot>();
        val offset = new Rect2D(x + parent.x, y + parent.y, width, height);
        for (val slot : slots) {
            mineSlots.addAll(slot.getSlots(offset));
        }
        return mineSlots;
    }

    private void verifyState(StitcherState... allowedStates) {
        boolean ok = false;
        for (val state : allowedStates) {
            if (state == this.state) {
                ok = true;
                break;
            }
        }
        if (!ok) {
            throw new IllegalStateException("Cold not execute operation: invalid state");
        }
    }
}
