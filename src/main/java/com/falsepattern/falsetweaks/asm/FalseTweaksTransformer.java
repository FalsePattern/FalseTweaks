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
import com.falsepattern.falsetweaks.asm.modules.threadedupdates.settings.Threading_GameSettings;
import com.falsepattern.falsetweaks.asm.modules.threadedupdates.settings.Threading_GameSettingsRedirector;
import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.falsetweaks.config.ThreadingConfig;
import com.falsepattern.lib.asm.ASMUtil;
import com.falsepattern.lib.asm.IClassNodeTransformer;
import com.falsepattern.lib.asm.SmartTransformer;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Getter
@Accessors(fluent = true)
public class FalseTweaksTransformer implements SmartTransformer {
    public static RenderGlobalDeOptimizer OPTIFINE_DEOPTIMIZER = new RenderGlobalDeOptimizer();
    public static final List<IClassNodeTransformer> TRANSFORMERS = new ArrayList<>(Collections.singletonList(OPTIFINE_DEOPTIMIZER));

    static {
        if (ModuleConfig.THREADED_CHUNK_UPDATES()) {
            TRANSFORMERS.add(new Threading_RenderBlocksASM());
            if (ThreadingConfig.TESSELLATOR_USE_REPLACEMENT_TARGETS.length > 0) {
                TRANSFORMERS.add(new Threading_TessellatorUseReplacement());
            }
            if (ThreadingConfig.THREAD_SAFE_ISBRHS.length > 0) {
                TRANSFORMERS.add(new Threading_ThreadSafeBlockRendererInjector());
            }
            TRANSFORMERS.add(new Threading_BlockMinMax());
            TRANSFORMERS.add(new Threading_BlockMinMaxRedirector());
            TRANSFORMERS.add(new Threading_GameSettings());
            TRANSFORMERS.add(new Threading_GameSettingsRedirector());
        }
    }

    private final Logger logger = LogManager.getLogger(Tags.MODNAME + " ASM");
    private final List<IClassNodeTransformer> transformers = TRANSFORMERS;

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        val transformers = new ArrayList<IClassNodeTransformer>();
        val cn = ASMUtil.parseClass(bytes, 0);
        for (val transformer : transformers()) {
            if (transformer.shouldTransform(cn, transformedName, CoreLoadingPlugin.isObfuscated())) {
                transformers.add(transformer);
            }
        }
        if (transformers.isEmpty()) {
            return bytes;
        }
        transformers.sort(Comparator.comparingInt(IClassNodeTransformer::internalSortingOrder));
        val log = logger();
        boolean changed = false;
        for (val transformer : transformers) {
            try {
                boolean applied;
                if (transformer instanceof ICancellableClassNodeTransformer) {
                    applied = ((ICancellableClassNodeTransformer) transformer).transformCancellable(cn, transformedName, CoreLoadingPlugin.isObfuscated());
                } else {
                    transformer.transform(cn, transformedName, CoreLoadingPlugin.isObfuscated());
                    applied = true;
                }
                if (applied) {
                    changed = true;
                    log.debug("Patched {} with {}...", transformedName, transformer.getName());
                }
            } catch (RuntimeException | Error t) {
                log.error("Error transforming {} with {}: {}", transformedName, transformer.getName(), t.getMessage());
                throw t;
            } catch (Throwable t) {
                log.error("Error transforming {} with {}: {}", transformedName, transformer.getName(), t.getMessage());
                throw new RuntimeException(t);
            }
        }
        if (changed) {
            val result = ASMUtil.serializeClass(cn, 0);
            log.debug("Patched {} successfully.", transformedName);
            return result;
        } else {
            return bytes;
        }
    }
}
