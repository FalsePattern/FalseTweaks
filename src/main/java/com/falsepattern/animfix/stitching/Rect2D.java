package com.falsepattern.animfix.stitching;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Rect2D {
    public int x;
    public int y;
    public int width;
    public int height;

    public void applyOffset(int x, int y) {
        this.x += x;
        this.y += y;
    }
}
