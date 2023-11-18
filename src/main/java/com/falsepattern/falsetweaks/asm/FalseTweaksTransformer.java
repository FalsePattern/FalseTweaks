/*
 * FalseTweaks
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

package com.falsepattern.falsetweaks.asm;

import com.falsepattern.falsetweaks.Share;
import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.falsetweaks.asm.modules.occlusion.optifine.RenderGlobalDeOptimizer;
import com.falsepattern.lib.asm.IClassNodeTransformer;
import com.falsepattern.lib.asm.SmartTransformer;
import com.falsepattern.lib.optifine.OptiFineTransformerHooks;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Accessors(fluent = true)
public class FalseTweaksTransformer implements SmartTransformer {
    private final Logger logger = LogManager.getLogger(Tags.MODNAME + " ASM");

    static {
        OptiFineTransformerHooks.disableOptiFinePatch("blo");
    }

    public static RenderGlobalDeOptimizer OPTIFINE_DEOPTIMIZER = new RenderGlobalDeOptimizer();
    public static final List<IClassNodeTransformer> TRANSFORMERS = new ArrayList<>(Arrays.asList(
            OPTIFINE_DEOPTIMIZER
                                                                                         ));

    private final List<IClassNodeTransformer> transformers = TRANSFORMERS;
}
