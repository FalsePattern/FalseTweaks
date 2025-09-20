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

package com.falsepattern.falsetweaks.modules.beddium;

import com.falsepattern.falsetweaks.api.dynlights.FTDynamicLights;
import com.ventooth.beddium.api.cache.SimpleChunkCache;
import com.ventooth.beddium.api.task.WorldRenderRegion;
import lombok.var;

import net.minecraft.world.World;

public class ChunkCacheBediumDynLights extends SimpleChunkCache {
    public ChunkCacheBediumDynLights(World world, WorldRenderRegion region) {
        super(world, region);
    }

    @Override
    protected int getLightBrightnessForSkyBlocksUncached(int x, int y, int z, int lightValue) {
        int light = super.getLightBrightnessForSkyBlocksUncached(x, y, z, lightValue);
        var dl = FTDynamicLights.frontend();
        if (dl.enabled() &&
            !this.getBlock(x, y, z)
                 .isOpaqueCube()) {
            light = dl.getCombinedLight(x, y, z, light);
        }

        return light;
    }
}
