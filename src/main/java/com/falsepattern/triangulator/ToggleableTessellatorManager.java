package com.falsepattern.triangulator;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true,
        chain = false)
public class ToggleableTessellatorManager {
    public static final ToggleableTessellatorManager INSTANCE = new ToggleableTessellatorManager();

    @Getter
    private int forceQuadRendering = 0;

    public void disableTriangulator() {
        forceQuadRendering++;
    }

    public void enableTriangulator() {
        forceQuadRendering--;
        if (forceQuadRendering < 0) {
            forceQuadRendering = 0;
        }
    }

    public boolean isTriangulatorDisabled() {
        return !TriCompat.enableTriangulation() || forceQuadRendering == 0;
    }
}
