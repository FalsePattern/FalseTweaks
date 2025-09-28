/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2025 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalseTweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * FalseTweaks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalseTweaks. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.falsetweaks.modules.triangulator.renderblocks;

import org.joml.Vector3i;
import org.joml.Vector3ic;

import net.minecraft.client.renderer.RenderBlocks;

public enum Facing {
    // @formatter:off
    YNEG(Direction.FACE_YNEG,
         (rb) -> rb.renderMinY <= 0,
         0.5f,
         new Vector3i(0, 0, 1), Direction.FACE_ZPOS,
         new Vector3i(1, 0, 0), Direction.FACE_XPOS,
         new Vector3i(0, 0, -1), Direction.FACE_ZNEG,
         new Vector3i(-1, 0, 0), Direction.FACE_XNEG,
         new Vector3i(0, -1, 0)),
    YPOS(Direction.FACE_YPOS,
         (rb) -> rb.renderMaxY >= 1,
         1f,
         new Vector3i(0, 0, 1), Direction.FACE_ZPOS,
         new Vector3i(-1, 0, 0), Direction.FACE_XNEG,
         new Vector3i(0, 0, -1), Direction.FACE_ZNEG,
         new Vector3i(1, 0, 0), Direction.FACE_XPOS,
         new Vector3i(0, 1, 0)),
    ZNEG(Direction.FACE_ZNEG,
         3,
         (rb) -> rb.renderMinZ <= 0,
         0.8f,
         new Vector3i(-1, 0, 0), Direction.FACE_XNEG,
         new Vector3i(0, -1, 0), Direction.FACE_YNEG,
         new Vector3i(1, 0, 0), Direction.FACE_XPOS,
         new Vector3i(0, 1, 0), Direction.FACE_YPOS,
         new Vector3i(0, 0, -1)),
    ZPOS(Direction.FACE_ZPOS,
         0,
         (rb) -> rb.renderMaxZ >= 1,
         0.8f,
         new Vector3i(0, 1, 0), Direction.FACE_YPOS,
         new Vector3i(1, 0, 0), Direction.FACE_XPOS,
         new Vector3i(0, -1, 0), Direction.FACE_YNEG,
         new Vector3i(-1, 0, 0), Direction.FACE_XNEG,
         new Vector3i(0, 0, 1)),
    XNEG(Direction.FACE_XNEG,
         3,
         (rb) -> rb.renderMinX <= 0,
         0.6f,
         new Vector3i(0, 0, 1), Direction.FACE_ZPOS,
         new Vector3i(0, -1, 0), Direction.FACE_YNEG,
         new Vector3i(0, 0, -1), Direction.FACE_ZNEG,
         new Vector3i(0, 1, 0), Direction.FACE_YPOS,
         new Vector3i(-1, 0, 0)),
    XPOS(Direction.FACE_XPOS,
         1,
         (rb) -> rb.renderMaxX >= 1,
         0.6f,
         new Vector3i(0, 0, 1), Direction.FACE_ZPOS,
         new Vector3i(0, 1, 0), Direction.FACE_YPOS,
         new Vector3i(0, 0, -1), Direction.FACE_ZNEG,
         new Vector3i(0, -1, 0), Direction.FACE_YNEG,
         new Vector3i(1, 0, 0));
    // @formatter:on
    public final Direction face;
    public final ShiftFunc shiftFunc;
    public final int worldUp;
    public final float brightness;
    public final Direction topFace;
    public final Vector3ic top;
    public final Vector3ic topRight;
    public final Direction rightFace;
    public final Vector3ic right;
    public final Vector3ic bottomRight;
    public final Direction bottomFace;
    public final Vector3ic bottom;
    public final Vector3ic bottomLeft;
    public final Direction leftFace;
    public final Vector3ic left;
    public final Vector3ic topLeft;
    public final Vector3ic front;

    Facing(Direction face,
           int worldUp,
           ShiftFunc shiftFunc,
           float brightness,
           Vector3ic top,
           Direction topFace,
           Vector3ic right,
           Direction rightFace,
           Vector3ic bottom,
           Direction bottomFace,
           Vector3ic left,
           Direction leftFace,
           Vector3ic front) {
        this.face = face;
        this.worldUp = worldUp;
        this.shiftFunc = shiftFunc;
        this.brightness = brightness;
        this.top = top;
        this.topFace = topFace;
        this.right = right;
        this.rightFace = rightFace;
        this.bottom = bottom;
        this.bottomFace = bottomFace;
        this.left = left;
        this.leftFace = leftFace;
        this.front = front;
        topLeft = new Vector3i(top).add(left);
        topRight = new Vector3i(top).add(right);
        bottomRight = new Vector3i(bottom).add(right);
        bottomLeft = new Vector3i(bottom).add(left);
    }

    Facing(Direction face,
           ShiftFunc shiftFunc,
           float brightness,
           Vector3ic top,
           Direction topFace,
           Vector3ic right,
           Direction rightFace,
           Vector3ic bottom,
           Direction bottomFace,
           Vector3ic left,
           Direction leftFace,
           Vector3ic front) {
        this(face, -1, shiftFunc, brightness, top, topFace, right, rightFace, bottom, bottomFace, left, leftFace, front);
    }

    public boolean shift(RenderBlocks rb) {
        return shiftFunc.doShift(rb);
    }

    public enum Direction {
        FACE_YNEG,
        FACE_YPOS,
        FACE_ZNEG,
        FACE_ZPOS,
        FACE_XNEG,
        FACE_XPOS
    }

    public interface ShiftFunc {
        boolean doShift(RenderBlocks rb);
    }
}
