package com.falsepattern.triangulator.mixin.helper;

import java.util.Comparator;

public interface ITessellatorMixin {
    void setAlternativeTriangulation();

    boolean isDrawingTris();

    boolean hackedQuadRendering();

    boolean quadTriangulationActive();

    boolean shaderOn();

    void shaderOn(boolean state);

    Comparator<?> hackQuadComparator(Comparator<?> comparator);

    int hackQuadCounting(int constant);

    void triangulate();
}
