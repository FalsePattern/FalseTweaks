package com.falsepattern.triangulator.renderblocks;

import org.joml.Vector3i;
import org.joml.Vector3ic;

import net.minecraft.client.renderer.RenderBlocks;

public enum Facing {
    YNEG(0,
         (rb) -> rb.renderMinY <= 0,
         0.5f,
         new Vector3i(0, 0, 1),
         new Vector3i(1, 0, 0),
         new Vector3i(0, 0, -1),
         new Vector3i(-1, 0, 0),
         new Vector3i(0, -1, 0)),
    YPOS(1,
         (rb) -> rb.renderMaxY >= 1,
         1f,
         new Vector3i(0, 0, 1),
         new Vector3i(-1, 0, 0),
         new Vector3i(0, 0, -1),
         new Vector3i(1, 0, 0),
         new Vector3i(0, 1, 0)),
    ZNEG(2,
         3,
         (rb) -> rb.renderMinZ <= 0,
         0.8f,
         new Vector3i(-1, 0, 0),
         new Vector3i(0, -1, 0),
         new Vector3i(1, 0, 0),
         new Vector3i(0, 1, 0),
         new Vector3i(0, 0, -1)),
    ZPOS(3,
         0,
         (rb) -> rb.renderMaxZ >= 1,
         0.8f,
         new Vector3i(0, 1, 0),
         new Vector3i(1, 0, 0),
         new Vector3i(0, -1, 0),
         new Vector3i(-1, 0, 0),
         new Vector3i(0, 0, 1)),
    XNEG(4,
         3,
         (rb) -> rb.renderMinX <= 0,
         0.6f,
         new Vector3i(0, 0, 1),
         new Vector3i(0, -1, 0),
         new Vector3i(0, 0, -1),
         new Vector3i(0, 1, 0),
         new Vector3i(-1, 0, 0)),
    XPOS(5,
         1,
         (rb) -> rb.renderMaxX >= 1,
         0.6f,
         new Vector3i(0, 0, 1),
         new Vector3i(0, 1, 0),
         new Vector3i(0, 0, -1),
         new Vector3i(0, -1, 0),
         new Vector3i(1, 0, 0));

    public interface ShiftFunc {
        boolean doShift(RenderBlocks rb);
    }

    public final int face;
    public final ShiftFunc shiftFunc;
    public final int worldUp;
    public final float brightness;
    public final Vector3ic top;
    public final Vector3ic topRight;
    public final Vector3ic right;
    public final Vector3ic bottomRight;
    public final Vector3ic bottom;
    public final Vector3ic bottomLeft;
    public final Vector3ic left;
    public final Vector3ic topLeft;
    public final Vector3ic front;

    Facing(int face, int worldUp, ShiftFunc shiftFunc, float brightness, Vector3ic top, Vector3ic right, Vector3ic bottom, Vector3ic left, Vector3ic front) {
        this.face = face;
        this.worldUp = worldUp;
        this.shiftFunc = shiftFunc;
        this.brightness = brightness;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
        this.front = front;
        topLeft = new Vector3i(top).add(left);
        topRight = new Vector3i(top).add(right);
        bottomRight = new Vector3i(bottom).add(right);
        bottomLeft = new Vector3i(bottom).add(left);
    }

    Facing(int face, ShiftFunc shiftFunc, float brightness, Vector3ic top, Vector3ic right, Vector3ic bottom, Vector3ic left, Vector3ic front) {
        this(face, -1, shiftFunc, brightness, top, right, bottom, left, front);
    }

    public boolean shift(RenderBlocks rb) {
        return shiftFunc.doShift(rb);
    }
}
