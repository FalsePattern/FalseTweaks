package com.falsepattern.animfix.stitching;

import com.falsepattern.animfix.stitching.packing2d.Algorithm;
import com.falsepattern.animfix.stitching.packing2d.Packer;
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
    private List<SpriteSlot> slots = new ArrayList<>();
    @Getter
    private StitcherState state = StitcherState.SETUP;

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
        width = nextPowerOfTwo(width);
        if (width > maxWidth) {
            throw new TooBigException();
        }
        width = Math.max(width >>> 1, 1);
        List<SpriteSlot> packedSlots;
        do {
            width *= 2;
            if (width > maxWidth) {
                throw new TooBigException();
            }
            packedSlots = Packer.pack(slots, Algorithm.FIRST_FIT_DECREASING_HEIGHT, width);
            height = 0;
            for (val sprite : packedSlots) {
                height = Math.max(height, sprite.y + sprite.height);
            }
            height = nextPowerOfTwo(height);

        } while (height > maxHeight || (height > width && width * 2 < maxWidth));
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
        for (val slot: slots) {
            mineSlots.addAll(slot.getSlots(offset));
        }
        return mineSlots;
    }

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
