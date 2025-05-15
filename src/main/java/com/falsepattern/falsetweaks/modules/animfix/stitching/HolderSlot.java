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

import com.falsepattern.falsetweaks.modules.animfix.interfaces.IStitcherSlotMixin;
import lombok.val;

import net.minecraft.client.renderer.texture.Stitcher;

import java.util.Collections;
import java.util.List;

public class HolderSlot extends SpriteSlot {
    private final Stitcher.Holder holder;

    public HolderSlot(Stitcher.Holder holder) {
        this.holder = holder;
        width = holder.getWidth();
        height = holder.getHeight();
    }

    @Override
    public List<Stitcher.Slot> getSlots(Rect2D parent) {
        val slot = new Stitcher.Slot(x + parent.x, y + parent.y, width, height);
        ((IStitcherSlotMixin) slot).insertHolder(holder);
        return Collections.singletonList(slot);
    }
}
