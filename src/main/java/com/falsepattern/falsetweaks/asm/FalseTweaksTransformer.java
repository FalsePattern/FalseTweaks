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

package com.falsepattern.falsetweaks.asm;

import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.falsetweaks.asm.modules.occlusion.optifine.RenderGlobalDeOptimizer;
import com.falsepattern.falsetweaks.asm.modules.threadedupdates.*;
import com.falsepattern.falsetweaks.asm.modules.threadedupdates.block.Threading_BlockMinMax;
import com.falsepattern.falsetweaks.asm.modules.threadedupdates.block.Threading_BlockMinMaxRedirector;
import com.falsepattern.falsetweaks.asm.modules.threadedupdates.compat.Threading_AngelicaCompatFixer;
import com.falsepattern.falsetweaks.asm.modules.threadedupdates.settings.Threading_GameSettings;
import com.falsepattern.falsetweaks.asm.modules.threadedupdates.settings.Threading_GameSettingsRedirector;
import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.falsetweaks.config.ThreadingConfig;
import com.falsepattern.falsetweaks.modules.threadedupdates.FastThreadLocal;
import com.falsepattern.lib.turboasm.MergeableTurboTransformer;
import com.falsepattern.lib.turboasm.TurboClassTransformer;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.List;

@Getter
@Accessors(fluent = true)
public class FalseTweaksTransformer extends MergeableTurboTransformer {
    static {
        try {
            RFBFixerUtility.removeGTNHLibHook();
        } catch (Throwable ignored) {}
    }
    private static List<TurboClassTransformer> transformers() {
        val transformers = new ArrayList<TurboClassTransformer>();
        if (FMLLaunchHandler.side().isClient()) {
            if (ModuleConfig.THREADED_CHUNK_UPDATES()) {
                transformers.add(new RenderGlobalDeOptimizer());
                transformers.add(new Threading_AngelicaCompatFixer());
                transformers.add(new Threading_RenderBlocksASM());
                transformers.add(new Threading_TessellatorUseReplacement());
                transformers.add(new Threading_ThreadSafeBlockRendererInjector());
            }
        }
        return transformers;
    }

    public FalseTweaksTransformer() {
        super(transformers());
        FastThreadLocal.setMainThread(Thread.currentThread());
    }
}
