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

import net.minecraft.client.renderer.WorldRenderer;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Provides a traversal order of the elements of RenderGlobal#worldRenderersToUpdate. Ideally, the order should be from
 * the closest renderer to the farthest.
 */
public interface IRendererUpdateOrderProvider {

    /**
     * Prepare providing a batch of renderers.
     */
    void prepare(List<WorldRenderer> worldRenderersToUpdateList, int updateLimit);

    boolean hasNext();

    WorldRenderer next();

    /**
     * End the batch. Remove the renderers that were provided during the batch from worldRenderersToUpdate
     */
    Future<List<WorldRenderer>> cleanup();

}
