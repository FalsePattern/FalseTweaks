/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2024 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalseTweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

public interface WorldRendererOcclusion {

    boolean ft$isInUpdateList();

    void ft$setInUpdateList(boolean b);

    int ft$currentPriority();

    void ft$currentPriority(int i);

    boolean ft$nextIsInFrustum();

    void ft$nextIsInFrustum(boolean b);

    OcclusionWorker.CullInfo ft$getCullInfo();

    boolean ft$needsSort();

    void ft$needsSort(boolean b);

    boolean ft$hasRenderList();

    boolean ft$genList();

    boolean ft$clearList();

    boolean ft$skipRenderPass();

    void ft$skipRenderPass(boolean value);

    void ft$updateNeighborCheckState(boolean isNonEmpty, int expected, int current, int posX, int posZ);

    boolean ft$hasAllNeighbors();

    boolean ft$isNonEmptyChunk();

    boolean ft$waitingOnShadowOcclusionQuery();

    void ft$waitingOnShadowOcclusionQuery(boolean value);

    boolean ft$isVisibleShadows();

    void ft$isVisibleShadows(boolean value);

    void ft$bumpFrustumCheckCanaryRender();
    void ft$bumpFrustumCheckCanaryShadow();

    int ft$frustumCheckCanaryRender();
    int ft$frustumCheckCanaryShadow();
}
