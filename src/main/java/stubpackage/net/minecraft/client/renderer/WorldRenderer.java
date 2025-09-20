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

package stubpackage.net.minecraft.client.renderer;

import net.minecraft.world.World;

import java.util.List;

public class WorldRenderer extends net.minecraft.client.renderer.WorldRenderer {
    /**
     * OptiFine-added field
     */
    public boolean needsBoxUpdate;

    public WorldRenderer(World p_i1240_1_,
                         List p_i1240_2_,
                         int p_i1240_3_,
                         int p_i1240_4_,
                         int p_i1240_5_,
                         int p_i1240_6_) {
        super(p_i1240_1_, p_i1240_2_, p_i1240_3_, p_i1240_4_, p_i1240_5_, p_i1240_6_);
    }
}
