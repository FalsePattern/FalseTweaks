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
package com.falsepattern.falsetweaks.mixin.mixins.common.misc;

import lombok.val;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.world.biome.BiomeGenBase;

@Mixin(BiomeGenBase.class)
public abstract class GetBiomeCrashFix_BiomeGenBaseMixin {
    @Shadow
    @Final
    public static BiomeGenBase ocean;
    @Shadow
    @Final
    private static BiomeGenBase[] biomeList;
    @Shadow
    @Final
    private static Logger logger;

    /**
     * return the biome specified by biomeID, or 0 (ocean) if out of bounds
     *
     * @author FalsePattern
     * @reason Null-safety
     */
    @Overwrite
    public static BiomeGenBase getBiome(int id) {
        if (id < 0 || id > biomeList.length) {
            logger.warn("Biome ID is out of bounds: " + id + ", defaulting to 0 (Ocean)");
            return ocean;
        }
        val biome = biomeList[id];
        if (biome != null) {
            return biome;
        }
        logger.warn("Biome ID is null: " + id + ", defaulting to 0 (Ocean)");
        return ocean;
    }
}
