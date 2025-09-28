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

package com.falsepattern.falsetweaks.modules.bsp.sorting;

import com.falsepattern.falsetweaks.Compat;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import lombok.val;
import org.joml.Math;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

//BSP sorter ported from MineTest
//https://github.com/minetest/minetest/blob/master/src/client/mapblock_mesh.cpp
public class ChunkBSPTree {
    private static final ThreadLocal<Scratch> SCRATCH = ThreadLocal.withInitial(Scratch::new);
    public final PolygonHolder polygonHolder;
    public final TIntArrayList polygonList = new TIntArrayList();
    private final List<TreeNode> nodes = new ArrayList<>();
    private int root = -1;

    public ChunkBSPTree(boolean triangleMode, Compat.ShaderType shaderType) {
        polygonHolder = new PolygonHolder(triangleMode, shaderType);
    }

    private static int findSplitCandidate(TIntList list,
                                          int start,
                                          int length,
                                          PolygonHolder holder,
                                          Vector3f scratchBuffer) {
        float centerX = 0;
        float centerY = 0;
        float centerZ = 0;
        for (int j = 0; j < length; j++) {
            val i = list.get(start + j);
            holder.midpoint(i, scratchBuffer);
            centerX += scratchBuffer.x;
            centerY += scratchBuffer.y;
            centerZ += scratchBuffer.z;
        }
        centerX /= length;
        centerY /= length;
        centerZ /= length;

        int candidatePolygon = list.get(start);
        float candidateArea = holder.area(candidatePolygon, scratchBuffer);
        holder.midpoint(candidatePolygon, scratchBuffer);
        float candidateMidpointX = scratchBuffer.x;
        float candidateMidpointY = scratchBuffer.y;
        float candidateMidpointZ = scratchBuffer.z;
        for (int j = 1; j < length; j++) {
            val polygon = list.get(start + j);
            val polygonArea = holder.area(polygon, scratchBuffer);
            holder.midpoint(polygon, scratchBuffer);
            float polygonMidpointX = scratchBuffer.x;
            float polygonMidpointY = scratchBuffer.y;
            float polygonMidpointZ = scratchBuffer.z;
            if (polygonArea > candidateArea ||
                (polygonArea == candidateArea &&
                 SharedMath.distanceSquared(polygonMidpointX,
                                            polygonMidpointY,
                                            polygonMidpointZ,
                                            centerX,
                                            centerY,
                                            centerZ) <
                 SharedMath.distanceSquared(candidateMidpointX,
                                            candidateMidpointY,
                                            candidateMidpointZ,
                                            centerX,
                                            centerY,
                                            centerZ))) {
                candidatePolygon = polygon;
                candidateArea = polygonArea;
                candidateMidpointX = polygonMidpointX;
                candidateMidpointY = polygonMidpointY;
                candidateMidpointZ = polygonMidpointZ;
            }
        }
        return candidatePolygon;
    }

    public void buildTree(int[] vertexData) {
        polygonHolder.setVertexData(vertexData);
        nodes.clear();
        val polygonCount = polygonHolder.getPolygonCount();
        val indexes = new TIntArrayList(polygonCount);
        for (int i = 0; i < polygonCount; i++) {
            indexes.add(i);
        }
        if (!indexes.isEmpty()) {
            val scratch = SCRATCH.get();
            root = buildTree(1,
                             0,
                             0,
                             8,
                             8,
                             8,
                             4,
                             indexes,
                             0,
                             indexes.size(),
                             scratch.nodeList,
                             scratch.frontList,
                             scratch.backList,
                             0,
                             scratch.scratchBuffer);
        } else {
            root = -1;
        }
    }

    public void traverse(Vector3f viewpoint) {
        polygonList.resetQuick();
        traverse(root, viewpoint, polygonList);
    }

    @SuppressWarnings({"UnnecessaryLocalVariable", "SuspiciousNameCombination"})
    //Code ported from MineTest, trying to keep parity
    private int buildTree(float normalX,
                          float normalY,
                          float normalZ,
                          float originX,
                          float originY,
                          float originZ,
                          float delta,
                          TIntArrayList list,
                          int start,
                          int length,
                          TIntArrayList nodeList,
                          TIntArrayList frontList,
                          TIntArrayList backList,
                          int depth,
                          Vector3f scratchBuffer) {
        if (length == 0) {
            return -1;
        }

        if (length == 1 || delta < 0.001) {
            nodes.add(new TreeNode(normalX, normalY, normalZ, originX, originY, originZ, list, start, length, -1, -1));
            return nodes.size() - 1;
        }

        for (int j = 0; j < length; j++) {
            val i = list.get(j + start);
            polygonHolder.midpoint(i, scratchBuffer);
            float midpointX = scratchBuffer.x;
            float midpointY = scratchBuffer.y;
            float midpointZ = scratchBuffer.z;
            float factor = SharedMath.dot(normalX,
                                          normalY,
                                          normalZ,
                                          midpointX - originX,
                                          midpointY - originY,
                                          midpointZ - originZ);
            if (factor == 0) {
                nodeList.add(i);
            } else if (factor > 0) {
                frontList.add(i);
            } else {
                backList.add(i);
            }
        }

        int nodeStart = start;
        int nodeLength = nodeList.size();
        for (int i = 0; i < nodeLength; i++) {
            list.setQuick(nodeStart + i, nodeList.getQuick(i));
        }
        nodeList.resetQuick();
        int frontStart = nodeStart + nodeLength;
        int frontLength = frontList.size();
        for (int i = 0; i < frontLength; i++) {
            list.setQuick(frontStart + i, frontList.getQuick(i));
        }
        frontList.resetQuick();
        int backStart = frontStart + frontLength;
        int backLength = backList.size();
        for (int i = 0; i < backLength; i++) {
            list.setQuick(backStart + i, backList.getQuick(i));
        }
        backList.resetQuick();

        float candidateNormalX = normalZ;
        float candidateNormalY = normalX;
        float candidateNormalZ = normalY;
        float candidateDelta = delta;
        if (depth % 3 == 2) {
            candidateDelta /= 2;
        }

        int frontIndex = -1;
        int backIndex = -1;

        if (frontLength != 0) {
            float nextNormalX = candidateNormalX;
            float nextNormalY = candidateNormalY;
            float nextNormalZ = candidateNormalZ;
            float nextOriginX = Math.fma(delta, normalX, originX);
            float nextOriginY = Math.fma(delta, normalY, originY);
            float nextOriginZ = Math.fma(delta, normalZ, originZ);
            float nextDelta = candidateDelta;
            if (nextDelta < 5) {
                val candidate = findSplitCandidate(list, frontStart, frontLength, polygonHolder, scratchBuffer);
                polygonHolder.normal(candidate, scratchBuffer);
                nextNormalX = scratchBuffer.x;
                nextNormalY = scratchBuffer.y;
                nextNormalZ = scratchBuffer.z;
                polygonHolder.midpoint(candidate, scratchBuffer);
                nextOriginX = scratchBuffer.x;
                nextOriginY = scratchBuffer.y;
                nextOriginZ = scratchBuffer.z;
            }
            frontIndex = buildTree(nextNormalX,
                                   nextNormalY,
                                   nextNormalZ,
                                   nextOriginX,
                                   nextOriginY,
                                   nextOriginZ,
                                   nextDelta,
                                   list,
                                   frontStart,
                                   frontLength,
                                   nodeList,
                                   frontList,
                                   backList,
                                   depth + 1,
                                   scratchBuffer);

            // if there are no other triangles, don't create a new node
            if (backLength == 0 && nodeLength == 0) {
                return frontIndex;
            }
        }

        if (backLength != 0) {
            float nextNormalX = candidateNormalX;
            float nextNormalY = candidateNormalY;
            float nextNormalZ = candidateNormalZ;
            float nextOriginX = Math.fma(-delta, normalX, originX);
            float nextOriginY = Math.fma(-delta, normalY, originY);
            float nextOriginZ = Math.fma(-delta, normalZ, originZ);
            float nextDelta = candidateDelta;
            if (nextDelta < 5) {
                val candidate = findSplitCandidate(list, backStart, backLength, polygonHolder, scratchBuffer);
                polygonHolder.normal(candidate, scratchBuffer);
                nextNormalX = scratchBuffer.x;
                nextNormalY = scratchBuffer.y;
                nextNormalZ = scratchBuffer.z;
                polygonHolder.midpoint(candidate, scratchBuffer);
                nextOriginX = scratchBuffer.x;
                nextOriginY = scratchBuffer.y;
                nextOriginZ = scratchBuffer.z;
            }

            backIndex = buildTree(nextNormalX,
                                  nextNormalY,
                                  nextNormalZ,
                                  nextOriginX,
                                  nextOriginY,
                                  nextOriginZ,
                                  nextDelta,
                                  list,
                                  backStart,
                                  backLength,
                                  nodeList,
                                  frontList,
                                  backList,
                                  depth + 1,
                                  scratchBuffer);

            // if there are no other triangles, don't create a new node
            if (frontLength == 0 && nodeLength == 0) {
                return backIndex;
            }
        }

        nodes.add(new TreeNode(normalX,
                               normalY,
                               normalZ,
                               originX,
                               originY,
                               originZ,
                               list,
                               nodeStart,
                               nodeLength,
                               frontIndex,
                               backIndex));

        return nodes.size() - 1;
    }

    private void traverse(int node, Vector3f viewpoint, TIntList output) {
        if (node < 0) {
            return;
        }

        val n = nodes.get(node);
        float factor = SharedMath.dot(n.normalX,
                                      n.normalY,
                                      n.normalZ,
                                      viewpoint.x - n.originX,
                                      viewpoint.y - n.originY,
                                      viewpoint.z - n.originZ);
        if (factor > 0) {
            traverse(n.backRef, viewpoint, output);
        } else {
            traverse(n.frontRef, viewpoint, output);
        }

        if (factor != 0) {
            for (int j = 0; j < n.length; j++) {
                output.add(n.triangleRefsList.get(j + n.start));
            }
        }

        if (factor > 0) {
            traverse(n.frontRef, viewpoint, output);
        } else {
            traverse(n.backRef, viewpoint, output);
        }
    }

    private static class Scratch {
        public final TIntArrayList nodeList = new TIntArrayList();
        public final TIntArrayList frontList = new TIntArrayList();
        public final TIntArrayList backList = new TIntArrayList();
        public final Vector3f scratchBuffer = new Vector3f();
    }
}
