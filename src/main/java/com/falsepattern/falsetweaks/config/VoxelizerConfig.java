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

import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.falsetweaks.modules.voxelizer.strategy.StrategyPreset;
import com.falsepattern.lib.config.Config;
import com.falsepattern.lib.config.ConfigurationManager;
import lombok.val;

import net.minecraft.launchwrapper.Launch;

import java.util.HashSet;
import java.util.Set;

@Config.Comment("Options for the FalseTweaks 3D item renderer framework")
@Config(modid = Tags.MOD_ID,
        category = "voxelizer")
@Config.LangKey
@Config.RequiresMcRestart
public class VoxelizerConfig {
    @Config.Ignore
    private static final Set<String> knownExcludes = new HashSet<>();
    @Config.Ignore
    private static final Set<String> knownIncludes = new HashSet<>();
    @Config.Ignore
    private static final Set<Class<?>> knownClassExcludes = new HashSet<>();
    @Config.Ignore
    private static final Set<Class<?>> knownClassIncludes = new HashSet<>();

    @Config.Comment({"You can use this property to fix any incorrectly detected overlays.",
                     "Syntax: texture_name=layer, where layer is the multiplier.",
                     "The default behaviour is that if the texture name ends with _overlay, its layer is set to 1,",
                     "This can be used to override that.",
                     "For reference: layer 0 is regular rendering, layer 1 is on top layer 0, layer 2 is on top of layer 1, etc.",
                     "Also supports negatives, but going below -1 is undefined behaviour. (-1 is used for the liquid inside potions by default)"})
    @Config.LangKey
    @Config.Name(value = "forcedLayers",
                 migrations = "")
    @Config.DefaultStringList({"potion_overlay=-1"})
    public static String[] FORCED_LAYERS;

    @Config.Comment({"The merging strategy preset to use for the voxelized mesh optimization.",
                     "Set this higher if you have a strong cpu and weak gpu, and set this lower if you have a weak cpu and strong gpu.",
                     "FPS impact: Depends on setup."})
    @Config.LangKey
    @Config.Name(value = "optimizationStrategy",
                 migrations = "")
    @Config.DefaultEnum("Best_2")
    public static StrategyPreset MESH_OPTIMIZATION_STRATEGY_PRESET;

    @Config.Comment({"Makes rails 3-dimensional. Doesn't require game restart.", "FPS impact: basically none"})
    @Config.LangKey
    @Config.Name(value = "rails3D",
                 migrations = "")
    @Config.DefaultBoolean(true)
    public static boolean RAILS_3D;

    @Config.Comment({"If set to true, the mesh compiler will print out detailed information when textures are compiled",
                     "into meshes."})
    @Config.LangKey
    @Config.Name(value = "debugMeshCompilation",
                 migrations = "")
    @Config.DefaultBoolean(false)
    public static boolean DEBUG_MESH_COMPILATION;

    @Config.Comment({"Item textures to disable voxelization for. Used to fix issues with certain items.",
                     "This is a PREFIX check, so you can also just specify a mod ID, and all items from it will skip getting voxelized.",
                     "Needs a game restart to apply changes (cached for performance).",
                     "Syntax: modid:texturename"})
    @Config.LangKey
    @Config.Name(value = "exclusionList",
                 migrations = "")
    @Config.DefaultStringList({"avaritia:infinity"})
    public static String[] EXCLUSION_LIST;

    @Config.Comment({"Classes to disable voxelization for. Used to fix issues with certain items.",
                     "This is an instanceof check, so superclasses are also checked.",
                     "Needs a game restart to apply changes (cached for performance)."})
    @Config.LangKey
    @Config.Name(value = "classExclusionList",
                 migrations = "")
    @Config.DefaultStringList({"cofh.lib.render.IFluidOverlayItem"})
    public static String[] CLASS_EXCLUSION_LIST;

    @Config.Comment({"The thickness of the 3D rails. Doesn't require game restart. 1 is vanilla thickness.",
                     "FPS impact: basically none"})
    @Config.LangKey
    @Config.Name(value = "railThickness",
                 migrations = "")
    @Config.DefaultDouble(1)
    public static double RAIL_THICKNESS;

    @Config.Comment({"Extremely verbose debug logging. This will spam your log.",
                     "Only useful on resource pack reloads."})
    @Config.LangKey
    @Config.Name(value = "verboseLog",
                 migrations = "")
    @Config.DefaultBoolean(false)
    public static boolean VERBOSE_LOG;

    static {
        ConfigurationManager.selfInit();
    }

    public static boolean isExcluded(String textureName) {
        if (knownExcludes.contains(textureName)) {
            return true;
        }
        if (knownIncludes.contains(textureName)) {
            return false;
        }
        for (String s : EXCLUSION_LIST) {
            if (textureName.startsWith(s)) {
                knownExcludes.add(textureName);
                return true;
            }
        }
        knownIncludes.add(textureName);
        return false;
    }

    public static boolean isClassExcluded(Class<?> clazz) {
        if (knownClassExcludes.contains(clazz)) {
            return true;
        }
        if (knownClassIncludes.contains(clazz)) {
            return false;
        }
        for (String s : CLASS_EXCLUSION_LIST) {
            try {
                if (Launch.classLoader.getClassBytes(s) != null) {
                    val excluded = Class.forName(s);
                    if (excluded.isAssignableFrom(clazz)) {
                        knownClassExcludes.add(clazz);
                        return true;
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        knownClassIncludes.add(clazz);
        return false;
    }

    //This is here to make the static initializer run
    public static void init() {

    }
}
