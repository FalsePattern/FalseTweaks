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
package stubpackage;

import net.minecraft.block.Block;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;

import java.util.Arrays;

public class ChunkCacheOF extends ChunkCache {
    public ChunkCacheOF(World p_i1964_1_, int p_i1964_2_, int p_i1964_3_, int p_i1964_4_, int p_i1964_5_, int p_i1964_6_, int p_i1964_7_, int p_i1964_8_) {
        super(p_i1964_1_, p_i1964_2_, p_i1964_3_, p_i1964_4_, p_i1964_5_, p_i1964_6_, p_i1964_7_, p_i1964_8_);
    }

    public void renderStart() {
    }

    public void renderFinish() {
    }
}
