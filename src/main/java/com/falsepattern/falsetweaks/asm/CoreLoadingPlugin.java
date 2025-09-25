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

package com.falsepattern.falsetweaks.asm;

import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.falsetweaks.mixin.plugin.standard.Mixin;
import com.falsepattern.falsetweaks.modules.animfix.AnimFixCompat;
import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadingCompat;
import com.falsepattern.falsetweaks.modules.threading.MainThreadContainer;
import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import lombok.Getter;
import lombok.val;
import org.spongepowered.asm.launch.GlobalProperties;
import org.spongepowered.asm.service.mojang.MixinServiceLaunchWrapper;

import net.minecraft.launchwrapper.IClassTransformer;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@IFMLLoadingPlugin.TransformerExclusions({Tags.ROOT_PKG + ".asm", "it.unimi.dsi.fastutil"})
public class CoreLoadingPlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {
    static IClassTransformer FIELD_HACK_TF;
    @Getter
    private static boolean obfuscated;

    static {
        val isClient = FMLLaunchHandler.side()
                                       .isClient();
        if (isClient) {
            MainThreadContainer.setAsMainThread();
        }
        ModuleConfig.init();

        if (isClient) {
            if (ModuleConfig.THREADED_CHUNK_UPDATES) {
                ThreadingCompat.executeConfigFixes();
                pleaseDontBreakMyThreadedRendering();
            }

            if (ModuleConfig.TEXTURE_OPTIMIZATIONS) {
                AnimFixCompat.executeConfigFixes();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void pleaseDontBreakMyThreadedRendering() {
        // Evil hack
        try {
            val theClass = Class.forName("com.gtnewhorizon.gtnhlib.core.transformer.TessellatorRedirectorTransformer");
            val exclusionsField = theClass.getDeclaredField("TransformerExclusions");
            exclusionsField.setAccessible(true);
            val exclusions = (List<String>) exclusionsField.get(null);
            exclusions.set(0, "");
        } catch (Throwable ignored) {
        }
    }

    @Override
    public String[] getASMTransformerClass() {
        val mixinTweakClasses = GlobalProperties.<List<String>>get(MixinServiceLaunchWrapper.BLACKBOARD_KEY_TWEAKCLASSES);
        if (mixinTweakClasses != null) {
            FIELD_HACK_TF = new FalseTweaksFieldHackTransformer();
            mixinTweakClasses.add(MixinCompatHackTweaker.class.getName());
        }
        val xFormers = new ArrayList<String>();
        if (FMLLaunchHandler.side()
                            .isClient() && ModuleConfig.THREADED_CHUNK_UPDATES()) {
            xFormers.add(Tags.ROOT_PKG + ".asm.modules.threadedupdates.compat.Threading_AngelicaRemapper");
        }
        xFormers.add(Tags.ROOT_PKG + ".asm.FalseTweaksTransformer");
        return xFormers.toArray(new String[0]);
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        obfuscated = (Boolean) data.get("runtimeDeobfuscationEnabled");
        //Doing this here because this runs after coremod init, but before minecraft classes start loading and mixins start colliding and crashing.

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public String getMixinConfig() {
        return "mixins.falsetweaks.early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        return IMixins.getEarlyMixins(Mixin.class, loadedCoreMods);
    }
}
