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

package com.falsepattern.triangulator.mixin.mixins.client.vanilla;

import com.falsepattern.triangulator.mixin.helper.IRenderBlocksMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;

@Mixin(RenderBlocks.class)
public abstract class RenderBlocksPerformanceMixin implements IRenderBlocksMixin {
    /**
     * @author FalsePattern
     * @reason Reimplement
     */
    @Overwrite
    public boolean renderStandardBlockWithAmbientOcclusion(Block block, int x, int y, int z, float r, float g, float b) {
        return renderWithAO(block, x, y, z, r, g, b);
    }

    /**
     * @author FalsePattern
     * @reason Reimplement
     */
    @Overwrite
    public boolean renderStandardBlockWithAmbientOcclusionPartial(Block block, int x, int y, int z, float r, float g, float b) {
        return renderWithAO(block, x, y, z, r, g, b);
    }
}
