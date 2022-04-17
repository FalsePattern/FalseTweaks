package com.falsepattern.animfix.interfaces;

public interface ITextureMapMixin {
    void initializeBatcher(int offsetX, int offsetY, int width, int height);

    String getBasePath();

    void disableBatching();
}
