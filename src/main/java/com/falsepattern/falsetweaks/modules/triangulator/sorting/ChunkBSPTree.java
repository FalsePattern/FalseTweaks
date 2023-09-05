/*
 * FalseTweaks
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

package com.falsepattern.falsetweaks.modules.triangulator.sorting;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import lombok.val;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

//BSP sorter ported from MineTest
//https://github.com/minetest/minetest/blob/master/src/client/mapblock_mesh.cpp
public class ChunkBSPTree {
    public final PolygonHolder polygonHolder;
    public final TIntList polygonList = new TIntArrayList();
    private List<TreeNode> nodes = new ArrayList<>();
    private int root = -1;

    public ChunkBSPTree(boolean triangleMode, boolean shaderMode) {
        polygonHolder = new PolygonHolder(triangleMode, shaderMode);
    }

    public void buildTree(int[] vertexData) {
        polygonHolder.setVertexData(vertexData);
        nodes.clear();
        val indexes = new TIntArrayList(polygonHolder.getPolygonCount());
        for (int i = 0; i < polygonHolder.getPolygonCount(); i++) {
            indexes.add(i);
        }
        if (!indexes.isEmpty()) {
            root = buildTree(new Vector3f(1, 0, 0), new Vector3f(8), 4, indexes, 0);
        } else {
            root = -1;
        }
    }

    public void traverse(Vector3f viewpoint) {
        polygonList.clear();
        traverse(root, viewpoint, polygonList);
    }

    private static int findSplitCandidate(TIntList list, PolygonHolder holder) {
        Vector3f center = new Vector3f();
        int n = list.size();
        Vector3f midpoint = new Vector3f();
        for (int j = 0; j < n; j++) {
            val i = list.get(j);
            holder.midpoint(i, midpoint);
            center.add(midpoint);
        }
        center.div(n);

        int candidatePolygon = list.get(0);
        float candidateArea = holder.area(candidatePolygon);
        Vector3f candidateMidpoint = new Vector3f();
        holder.midpoint(candidatePolygon, candidateMidpoint);
        for (int j = 0; j < n; j++) {
            val i = list.get(j);
            val area = holder.area(i);
            if (area > candidateArea ||
                (area == candidateArea &&
                 midpoint.distanceSquared(center) < candidateMidpoint.distanceSquared(center))) {
                candidatePolygon = i;
                candidateArea = area;
                candidateMidpoint.set(midpoint);
            }
        }
        return candidatePolygon;
    }

    private int buildTree(Vector3f normal, Vector3f origin, float delta, TIntList list, int depth) {
        if (list.isEmpty()) {
            return -1;
        }

        if (list.size() == 1 || delta < 0.001) {
            nodes.add(new TreeNode(normal, origin, list, -1, -1));
            return nodes.size() - 1;
        }

        val frontList = new TIntArrayList();
        val backList = new TIntArrayList();
        val nodeList = new TIntArrayList();

        Vector3f midpoint = new Vector3f();
        for (int j = 0, length = list.size(); j < length; j++) {
            val i = list.get(j);
            polygonHolder.midpoint(i, midpoint);
            float factor = normal.dot(midpoint.sub(origin));
            if (factor == 0) {
                nodeList.add(i);
            } else if (factor > 0) {
                frontList.add(i);
            } else {
                backList.add(i);
            }
        }

        Vector3f candidateNormal = new Vector3f(normal.z, normal.x, normal.y);
        float candidateDelta = delta;
        if (depth % 3 == 2) {
            candidateDelta /= 2;
        }

        int frontIndex = -1;
        int backIndex = -1;

        if (!frontList.isEmpty()) {
            val nextNormal = new Vector3f(candidateNormal);
            val nextOrigin = new Vector3f(delta).mul(normal).add(origin);
            float nextDelta = candidateDelta;
            if (nextDelta < 5) {
                val candidate = findSplitCandidate(frontList, polygonHolder);
                polygonHolder.normal(candidate, nextNormal);
                polygonHolder.midpoint(candidate, nextOrigin);
            }
            frontIndex = buildTree(nextNormal, nextOrigin, nextDelta, frontList, depth + 1);

            // if there are no other triangles, don't create a new node
            if (backList.isEmpty() && nodeList.isEmpty()) {
                return frontIndex;
            }
        }

        if (!backList.isEmpty()) {
            val nextNormal = new Vector3f(candidateNormal);
            val nextOrigin = new Vector3f(delta).add(normal).negate().add(origin);
            float nextDelta = candidateDelta;
            if (nextDelta < 5) {
                val candidate = findSplitCandidate(backList, polygonHolder);
                polygonHolder.normal(candidate, nextNormal);
                polygonHolder.midpoint(candidate, nextOrigin);
            }

            backIndex = buildTree(nextNormal, nextOrigin, nextDelta, backList, depth + 1);

            // if there are no other triangles, don't create a new node
            if (frontList.isEmpty() && nodeList.isEmpty()) {
                return backIndex;
            }
        }

        nodes.add(new TreeNode(normal, origin, nodeList, frontIndex, backIndex));

        return nodes.size() - 1;
    }

    private void traverse(int node, Vector3f viewpoint, TIntList output) {
        if (node < 0) return;

        val n = nodes.get(node);
        float factor = n.normal.dot(viewpoint.x - n.origin.x, viewpoint.y - n.origin.y, viewpoint.z - n.origin.z);
        if (factor > 0) {
            traverse(n.backRef, viewpoint, output);
        } else {
            traverse(n.frontRef, viewpoint, output);
        }

        if (factor != 0) {
            for (int j = 0, length = n.triangleRefs.size(); j < length; j++) {
                output.add(n.triangleRefs.get(j));
            }
        }

        if (factor > 0) {
            traverse(n.frontRef, viewpoint, output);
        } else {
            traverse(n.backRef, viewpoint, output);
        }
    }
}
