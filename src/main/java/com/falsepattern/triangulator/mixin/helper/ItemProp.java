package com.falsepattern.triangulator.mixin.helper;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ItemProp {
    private float a;
    private float b;
    private float c;
    private float d;
    private int e;
    private int f;
    private float g;

    public ItemProp(ItemProp old) {
        set(old.a, old.b, old.c, old.d, old.e, old.f, old.g);
    }

    public void set(float a, float b, float c, float d, int e, int f, float g) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
        this.g = g;
    }
}
