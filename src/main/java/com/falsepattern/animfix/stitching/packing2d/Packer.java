package com.falsepattern.animfix.stitching.packing2d;

import com.falsepattern.animfix.stitching.Rect2D;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class Packer<T extends Rect2D> {
    final int stripWidth;
    final List<T> rectangles;

    Packer(int stripWidth, List<T> rectangles) {
        this.stripWidth = stripWidth;
        this.rectangles = rectangles;
    }

    public static <U extends Rect2D> List<U> pack(List<U> rectangles, Algorithm algorithm, int stripWidth) {
        Packer<U> packer;
        switch (algorithm) {
            case FIRST_FIT_DECREASING_HEIGHT:
                packer = new PackerFFDH<>(stripWidth, rectangles);
                return packer.pack();
            case BEST_FIT_DECREASING_HEIGHT:
                packer = new PackerBFDH<>(stripWidth, rectangles);
                break;
            default:
                return new ArrayList<>();
        }
        return packer.pack();
    }

    public abstract List<T> pack();

    void sortByNonIncreasingHeight(List<T> rectangles) {
        rectangles.sort(Comparator.<T>comparingInt((rect) -> rect.height).reversed());
    }
}
