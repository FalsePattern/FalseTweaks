/*
 * Triangulator
 *
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice, this permission notice and the word "SNEED"
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.falsetweaks.voxelizer.strategy;

import com.falsepattern.falsetweaks.voxelizer.Face;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ExpandingRectMergingStrategy implements MergingStrategy {
    public static final ExpandingRectMergingStrategy NoFlipNoInvRD;
    public static final ExpandingRectMergingStrategy NoFlipYesInvRD;
    public static final ExpandingRectMergingStrategy YesFlipNoInvRD;
    public static final ExpandingRectMergingStrategy YesFlipYesInvRD;
    public static final ExpandingRectMergingStrategy NoFlipNoInvRU;
    public static final ExpandingRectMergingStrategy NoFlipYesInvRU;
    public static final ExpandingRectMergingStrategy YesFlipNoInvRU;
    public static final ExpandingRectMergingStrategy YesFlipYesInvRU;
    public static final ExpandingRectMergingStrategy NoFlipNoInvLD;
    public static final ExpandingRectMergingStrategy NoFlipYesInvLD;
    public static final ExpandingRectMergingStrategy YesFlipNoInvLD;
    public static final ExpandingRectMergingStrategy YesFlipYesInvLD;
    public static final ExpandingRectMergingStrategy NoFlipNoInvLU;
    public static final ExpandingRectMergingStrategy NoFlipYesInvLU;
    public static final ExpandingRectMergingStrategy YesFlipNoInvLU;
    public static final ExpandingRectMergingStrategy YesFlipYesInvLU;
    
    public static List<ExpandingRectMergingStrategy> all() {
        return Arrays.asList(NoFlipNoInvRD, NoFlipYesInvRD, YesFlipNoInvRD, YesFlipYesInvRD,
                             NoFlipNoInvRU, NoFlipYesInvRU, YesFlipNoInvRU, YesFlipYesInvRU,
                             NoFlipNoInvLD, NoFlipYesInvLD, YesFlipNoInvLD, YesFlipYesInvLD,
                             NoFlipNoInvLU, NoFlipYesInvLU, YesFlipNoInvLU, YesFlipYesInvLU);
    }


    static {
        Expander expandRight = new Expander() {
            @Override
            public boolean expand(Face[][] faces, Face root) {
                return root.maxX < faces[0].length - 1 && tryExpandRight(faces, root);
            }

            @Override
            public int startIndex(Face[][] faces) {
                return 0;
            }

            @Override
            public boolean shouldContinue(Face[][] faces, int index) {
                return index < faces[0].length;
            }

            @Override
            public int modifyValue(int index) {
                return index + 1;
            }
        };
        Expander expandDown = new Expander() {

            @Override
            public boolean expand(Face[][] faces, Face root) {
                return root.maxY < faces.length - 1 && tryExpandDown(faces, root);
            }

            @Override
            public int startIndex(Face[][] faces) {
                return 0;
            }

            @Override
            public boolean shouldContinue(Face[][] faces, int index) {
                return index < faces.length;
            }

            @Override
            public int modifyValue(int index) {
                return index + 1;
            }
        };
        Expander expandLeft = new Expander() {
            @Override
            public boolean expand(Face[][] faces, Face root) {
                return root.minX > 0 && tryExpandLeft(faces, root);
            }

            @Override
            public int startIndex(Face[][] faces) {
                return faces[0].length - 1;
            }

            @Override
            public boolean shouldContinue(Face[][] faces, int index) {
                return index >= 0;
            }

            @Override
            public int modifyValue(int index) {
                return index - 1;
            }
        };
        Expander expandUp = new Expander() {
            @Override
            public boolean expand(Face[][] faces, Face root) {
                return root.minY > 0 && tryExpandUp(faces, root);
            }

            @Override
            public int startIndex(Face[][] faces) {
                return faces.length - 1;
            }

            @Override
            public boolean shouldContinue(Face[][] faces, int index) {
                return index >= 0;
            }

            @Override
            public int modifyValue(int index) {
                return index - 1;
            }
        };
        val builder = builder();
        builder.inverseExpansion(false)
               .horizontalExpander(expandRight)
               .verticalExpander(expandDown);
        NoFlipNoInvRD = builder.flipIteration(false).build();
        NoFlipYesInvRD = builder.inverseExpansion(true).build();
        YesFlipYesInvRD = builder.flipIteration(true).build();
        YesFlipNoInvRD = builder.inverseExpansion(false).build();
        builder.verticalExpander(expandUp);
        NoFlipNoInvRU = builder.flipIteration(false).build();
        NoFlipYesInvRU = builder.inverseExpansion(true).build();
        YesFlipYesInvRU = builder.flipIteration(true).build();
        YesFlipNoInvRU = builder.inverseExpansion(false).build();
        builder.horizontalExpander(expandLeft);
        builder.verticalExpander(expandDown);
        NoFlipNoInvLD = builder.flipIteration(false).build();
        NoFlipYesInvLD = builder.inverseExpansion(true).build();
        YesFlipYesInvLD = builder.flipIteration(true).build();
        YesFlipNoInvLD = builder.inverseExpansion(false).build();
        builder.verticalExpander(expandUp);
        NoFlipNoInvLU = builder.flipIteration(false).build();
        NoFlipYesInvLU = builder.inverseExpansion(true).build();
        YesFlipYesInvLU = builder.flipIteration(true).build();
        YesFlipNoInvLU = builder.inverseExpansion(false).build();
    }
    private final Expander horizontalExpander;
    private final Expander verticalExpander;
    private final boolean flipIteration;
    private final boolean inverseExpansion;
    
    @Override
    public void merge(Face[][] faces) {
        if (flipIteration) {
            for (int x = horizontalExpander.startIndex(faces); horizontalExpander.shouldContinue(faces, x); x = horizontalExpander.modifyValue(x)) {
                for (int y = verticalExpander.startIndex(faces); verticalExpander.shouldContinue(faces, y); y = verticalExpander.modifyValue(y)) {
                    tryExpand(faces, x, y);
                }
            }
        } else {
            for (int y = verticalExpander.startIndex(faces); verticalExpander.shouldContinue(faces, y); y = verticalExpander.modifyValue(y)) {
                for (int x = horizontalExpander.startIndex(faces); horizontalExpander.shouldContinue(faces, x); x = horizontalExpander.modifyValue(x)) {
                    tryExpand(faces, x, y);
                }
            }
        }
    }

    private void tryExpand(Face[][] faces, int x, int y) {
        Face root = faces[y][x];
        if (root == null || root.parent != null) {
            return;
        }
        while (true) {
            boolean expanded;
            if (inverseExpansion) {
                expanded = verticalExpander.expand(faces, root);
                expanded |= horizontalExpander.expand(faces, root);
            } else {
                expanded = horizontalExpander.expand(faces, root);
                expanded |= verticalExpander.expand(faces, root);
            }
            if (!expanded) {
                break;
            }
        }
    }

    private static boolean tryExpandRight(Face[][] faces, Face root) {
        val candidates = new ArrayList<Face>();
        for (int y = root.minY; y <= root.maxY; y++) {
            Face candidate = faces[y][root.maxX + 1];
            if (isIneligible(candidate)) {
                return false;
            }
            candidates.add(candidate);
        }
        if (candidates.size() == 0) {
            return false;
        }
        mergeAll(candidates);
        Face.tryMerge(root, candidates.get(0));
        return true;
    }

    private static boolean tryExpandDown(Face[][] faces, Face root) {
        val candidates = new ArrayList<Face>();
        for (int x = root.minX; x <= root.maxX; x++) {
            Face candidate = faces[root.maxY + 1][x];
            if (isIneligible(candidate)) {
                return false;
            }
            candidates.add(candidate);
        }
        if (candidates.size() == 0) {
            return false;
        }
        mergeAll(candidates);
        Face.tryMerge(root, candidates.get(0));
        return true;
    }

    private static boolean tryExpandLeft(Face[][] faces, Face root) {
        val candidates = new ArrayList<Face>();
        for (int y = root.minY; y <= root.maxY; y++) {
            Face candidate = faces[y][root.minX - 1];
            if (isIneligible(candidate)) {
                return false;
            }
            candidates.add(candidate);
        }
        if (candidates.size() == 0) {
            return false;
        }
        mergeAll(candidates);
        Face.tryMerge(root, candidates.get(0));
        return true;
    }

    private static boolean tryExpandUp(Face[][] faces, Face root) {
        val candidates = new ArrayList<Face>();
        for (int x = root.minX; x <= root.maxX; x++) {
            Face candidate = faces[root.minY - 1][x];
            if (isIneligible(candidate)) {
                return false;
            }
            candidates.add(candidate);
        }
        if (candidates.size() == 0) {
            return false;
        }
        mergeAll(candidates);
        Face.tryMerge(root, candidates.get(0));
        return true;
    }

    private static void mergeAll(List<Face> candidates) {
        if (candidates.size() <= 1) {
            return;
        }
        Face root = candidates.get(0);
        for (val candidate: candidates.subList(1, candidates.size())) {
            Face.tryMerge(root, candidate);
        }
    }

    private static boolean isIneligible(Face face) {
        return face == null || face.parent != null;
    }
    
    private interface Expander {
        boolean expand(Face[][] faces, Face root);
        int startIndex(Face[][] faces);
        boolean shouldContinue(Face[][] faces, int index);
        int modifyValue(int index);
    }
}
