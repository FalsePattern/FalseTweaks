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
package com.falsepattern.falsetweaks.modules.occlusion;

import net.minecraft.util.EnumFacing;

import java.util.Iterator;
import java.util.Set;

public class SetVisibility {

    private static final int COUNT_FACES = OcclusionHelpers.FACING_VALUES.length;

    public static long setManyVisible(long bitSet, Set<EnumFacing> faces) {
        Iterator<EnumFacing> iterator = faces.iterator();

        while (iterator.hasNext()) {
            EnumFacing enumfacing = iterator.next();
            Iterator<EnumFacing> iterator1 = faces.iterator();

            while (iterator1.hasNext()) {
                EnumFacing enumfacing1 = iterator1.next();
                bitSet = setVisible(bitSet, enumfacing, enumfacing1, true);
            }
        }
        return bitSet;
    }

    public static long setVisible(long bitSet, EnumFacing from, EnumFacing to, boolean visible) {
        bitSet = setBit(bitSet, from.ordinal() + to.ordinal() * COUNT_FACES, visible);
        bitSet = setBit(bitSet, to.ordinal() + from.ordinal() * COUNT_FACES, visible);

        return bitSet;
    }

    private static long setBit(long bitSet, int index, boolean value) {
        if (value) {
            bitSet |= (1L << index);
        } else {
            bitSet &= ~(1L << index);
        }
        return bitSet;
    }

    public static boolean isVisible(long bitSet, EnumFacing from, EnumFacing to) {
        return from == null || to == null || (bitSet & (1L << (from.ordinal() + to.ordinal() * COUNT_FACES))) != 0;
    }

    public static String toString(long bitSet) {

        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append(' ');
        EnumFacing[] aenumfacing = OcclusionHelpers.FACING_VALUES;
        int i = aenumfacing.length;
        int j;
        EnumFacing enumfacing;

        for (j = 0; j < i; ++j) {
            enumfacing = aenumfacing[j];
            stringbuilder.append(' ').append(enumFacingToStringFixed(enumfacing).toUpperCase().charAt(0));
        }

        stringbuilder.append('\n');
        aenumfacing = OcclusionHelpers.FACING_VALUES;
        i = aenumfacing.length;

        for (j = 0; j < i; ++j) {
            enumfacing = aenumfacing[j];
            stringbuilder.append(enumFacingToStringFixed(enumfacing).toUpperCase().charAt(0));
            EnumFacing[] aenumfacing1 = OcclusionHelpers.FACING_VALUES;
            int k = aenumfacing1.length;

            for (int l = 0; l < k; ++l) {
                EnumFacing enumfacing1 = aenumfacing1[l];

                if (enumfacing == enumfacing1) {
                    stringbuilder.append("  ");
                } else {
                    boolean flag = isVisible(bitSet, enumfacing, enumfacing1);
                    stringbuilder.append(' ').append(flag ? 'Y' : 'n');
                }
            }

            stringbuilder.append('\n');
        }

        return stringbuilder.toString();
    }

    // Do not trust MCP.
    private static String enumFacingToStringFixed(EnumFacing f) {
        return new String[]{"DOWN", "UP", "NORTH", "SOUTH", "WEST", "EAST"}[f.ordinal()];
    }

}
