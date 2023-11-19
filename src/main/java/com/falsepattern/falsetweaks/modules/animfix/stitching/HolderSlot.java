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
