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

import com.falsepattern.falsetweaks.Compat;
import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.lib.config.ConfigException;
import com.falsepattern.lib.config.ConfigurationManager;
import lombok.val;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import cpw.mods.fml.client.config.DummyConfigElement;
import cpw.mods.fml.client.config.DummyConfigElement.DummyCategoryElement;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FalseTweaksGuiConfig extends GuiConfig {
    public FalseTweaksGuiConfig(GuiScreen parent) throws ConfigException {
        super(parent,
              getConfigElements(),
              Tags.MOD_ID,
              false,
              false,
              Tags.MOD_NAME + " Configuration"
              );
    }

    @SuppressWarnings({"rawtypes"})
    private static List<IConfigElement> getConfigElements() throws ConfigException {
        val result = new ArrayList<IConfigElement>();
        if (Compat.dynamicLightsPresent()) {
            result.add(category("dynlights", DynamicLightsConfig.class));
        }
        if (ModuleConfig.TRIANGULATOR()) {
            result.add(category("triangulator", TriangulatorConfig.class));
        }
        if (ModuleConfig.ITEM_RENDER_LISTS) {
            result.add(category("item_render_lists", RenderListConfig.class));
        }
        if (ModuleConfig.VOXELIZER) {
            result.add(category("voxelizer", VoxelizerConfig.class));
        }
        if (ModuleConfig.ADVANCED_PROFILER) {
            result.add(category("profiler", ProfilerConfig.class));
        }
        if (ModuleConfig.BLOCK_LAYER_TRANSPARENCY_FIX) {
            result.add(category("translucent_block_layers_fix", TranslucentBlockLayersConfig.class));
        }
        if (ModuleConfig.THREADED_CHUNK_UPDATES()) {
            result.add(category("occlusion", OcclusionConfig.class));
            result.add(category("threading", ThreadingConfig.class));
        }
        return result;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static IConfigElement category(String name, Class<?> klass) throws ConfigException {
        return new DummyCategoryElement(name,
                                        "config.falsetweaks." + name + ".category",
                                        ConfigurationManager.getConfigElementsMulti(klass));
    }
}
