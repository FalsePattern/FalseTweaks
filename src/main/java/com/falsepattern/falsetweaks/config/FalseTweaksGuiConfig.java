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

package com.falsepattern.falsetweaks.config;

import com.falsepattern.falsetweaks.Compat;
import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.lib.config.ConfigException;
import com.falsepattern.lib.config.SimpleGuiConfig;
import lombok.val;

import net.minecraft.client.gui.GuiScreen;

import java.util.ArrayList;

public class FalseTweaksGuiConfig extends SimpleGuiConfig {
    public FalseTweaksGuiConfig(GuiScreen parent) throws ConfigException {
        super(parent, Tags.MOD_ID, Tags.MOD_NAME, getConfigClasses());
    }

    private static Class<?>[] getConfigClasses() {
        val result = new ArrayList<Class<?>>();
        result.add(ModuleConfig.class);
        if (ModuleConfig.aoFix) {
            result.add(AOFixConfig.class);
        }
        if (ModuleConfig.blockCrackFix) {
            result.add(CrackFixConfig.class);
        }
        if (Compat.dynamicLightsPresent()) {
            result.add(DynamicLightsConfig.class);
        }
        if (ModuleConfig.OPTIFINE_LOGSPAM_FIX && Compat.optiFineHasShaders()) {
            result.add(OptiSpamConfig.class);
        }
        if (ModuleConfig.ADVANCED_PROFILER) {
            result.add(ProfilerConfig.class);
        }
        if (ModuleConfig.UNLOCK_RENDER_DISTANCE) {
            result.add(RenderDistanceConfig.class);
        }
        if (ModuleConfig.RENDERING_SAFETY) {
            result.add(RenderingSafetyConfig.class);
        }
        if (ModuleConfig.ITEM_RENDER_LISTS) {
            result.add(RenderListConfig.class);
        }
        if (ModuleConfig.THREADED_CHUNK_UPDATES()) {
            result.add(ThreadingConfig.class);
        }
        if (ModuleConfig.BLOCK_LAYER_TRANSPARENCY_FIX) {
            result.add(TranslucentBlockLayersConfig.class);
        }
        if (ModuleConfig.TRIANGULATOR) {
            result.add(TriangulatorConfig.class);
        }
        if (ModuleConfig.VOXELIZER) {
            result.add(VoxelizerConfig.class);
        }
        return result.toArray(new Class<?>[0]);
    }
}
