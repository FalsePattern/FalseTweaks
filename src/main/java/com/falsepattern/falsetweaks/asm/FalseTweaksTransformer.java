/*
 * This file is part of FalseTweaks.
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
import com.falsepattern.falsetweaks.asm.modules.threadedupdates.Threading_RenderBlocksASM;
import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.lib.asm.ASMUtil;
import com.falsepattern.lib.asm.IClassNodeTransformer;
import com.falsepattern.lib.asm.SmartTransformer;
import com.falsepattern.lib.optifine.OptiFineTransformerHooks;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Getter
@Accessors(fluent = true)
public class FalseTweaksTransformer implements SmartTransformer {
    private final Logger logger = LogManager.getLogger(Tags.MODNAME + " ASM");

    public static RenderGlobalDeOptimizer OPTIFINE_DEOPTIMIZER = new RenderGlobalDeOptimizer();
    public static final List<IClassNodeTransformer> TRANSFORMERS = new ArrayList<>(Arrays.asList(OPTIFINE_DEOPTIMIZER));

    static {
        if (ModuleConfig.THREADED_CHUNK_UPDATES()) {
            TRANSFORMERS.add(new Threading_RenderBlocksASM());
        }
    }

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
        for (val transformer : transformers) {
            log.debug("Patching {} with {}...", transformedName, transformer.getName());
            try {
                transformer.transform(cn, transformedName, CoreLoadingPlugin.isObfuscated());
            } catch (RuntimeException | Error t) {
                log.error("Error transforming {} with {}: {}", transformedName, transformer.getName(), t.getMessage());
                throw t;
            } catch (Throwable t) {
                log.error("Error transforming {} with {}: {}", transformedName, transformer.getName(), t.getMessage());
                throw new RuntimeException(t);
            }
        }
        val result = ASMUtil.serializeClass(cn, 0);
        log.debug("Patched {} successfully.", transformedName);
        return result;
    }

    private final List<IClassNodeTransformer> transformers = TRANSFORMERS;
}
