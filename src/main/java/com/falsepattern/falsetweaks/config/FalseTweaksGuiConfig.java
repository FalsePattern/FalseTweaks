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

package com.falsepattern.falsetweaks.config;

import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.lib.config.ConfigException;
import com.falsepattern.lib.config.SimpleGuiConfig;
import lombok.val;

import net.minecraft.client.gui.GuiScreen;

import java.util.ArrayList;

public class FalseTweaksGuiConfig extends SimpleGuiConfig {
    public FalseTweaksGuiConfig(GuiScreen parent) throws ConfigException {
        super(parent, Tags.MODID, Tags.MODNAME, fetchConfigClasses());
    }

    private static Class<?>[] fetchConfigClasses() {
        val result = new ArrayList<Class<?>>();
        if (ModuleConfig.TRIANGULATOR()) {
            result.add(TriangulatorConfig.class);
        }
        if (ModuleConfig.ITEM_RENDER_LISTS) {
            result.add(RenderListConfig.class);
        }
        if (ModuleConfig.VOXELIZER) {
            result.add(VoxelizerConfig.class);
        }
        if (ModuleConfig.ADVANCED_PROFILER) {
            result.add(ProfilerConfig.class);
        }
        if (ModuleConfig.BLOCK_LAYER_TRANSPARENCY_FIX) {
            result.add(TranslucentBlockLayersConfig.class);
        }
        if (ModuleConfig.THREADED_CHUNK_UPDATES()) {
            result.add(OcclusionConfig.class);
            result.add(ThreadingConfig.class);
        }
        return result.toArray(new Class<?>[0]);
    }
}
