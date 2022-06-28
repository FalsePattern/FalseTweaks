package com.falsepattern.animfix.stitching.packing2d;

import com.falsepattern.animfix.stitching.Rect2D;
import lombok.val;
import lombok.var;

import java.util.ArrayList;
import java.util.List;

public class PackerBFDH<T extends Rect2D> extends Packer<T> {
    private final List<StripLevel> levels;

    public PackerBFDH(int stripWidth, List<T> rectangles) {
        super(stripWidth, rectangles);
        levels = new ArrayList<>();
    }

    @Override
    public List<T> pack() {
        var top = 0;
        sortByNonIncreasingHeight(rectangles);
        for (val r : rectangles) {
            StripLevel levelWithSmallestResidual = null;
            for (val level : levels) {
                if (!level.canFit(r)) {
                    continue;
                }
                if (levelWithSmallestResidual != null && levelWithSmallestResidual.availableWidth() > level.availableWidth()) {
                    levelWithSmallestResidual = level;
                } else if (levelWithSmallestResidual == null) {
                    levelWithSmallestResidual = level;
                }
            }
            if (levelWithSmallestResidual == null) {
                val level = new StripLevel(stripWidth, top);
                level.fitRectangle(r);
                levels.add(level);
                top += r.height;
            } else {
                val newLevel = levelWithSmallestResidual.fitRectangle(r);
                if (newLevel != null) {
                    levels.add(levels.indexOf(levelWithSmallestResidual) + 1, newLevel);
                }
            }

        }
        return rectangles;
    }
}
