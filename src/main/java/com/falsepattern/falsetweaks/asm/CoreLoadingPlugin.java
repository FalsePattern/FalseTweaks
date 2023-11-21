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
import com.falsepattern.falsetweaks.config.ModuleConfig;
import com.falsepattern.falsetweaks.modules.occlusion.OcclusionCompat;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

@IFMLLoadingPlugin.TransformerExclusions(Tags.GROUPNAME + ".asm")
public class CoreLoadingPlugin implements IFMLLoadingPlugin {
    static {
        ModuleConfig.init();

        if (ModuleConfig.OCCLUSION_TWEAKS)
            OcclusionCompat.executeConfigCompatibilityHacks();
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{Tags.GROUPNAME + ".asm.FalseTweaksTransformer"};
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
        //Doing this here because this runs after coremod init, but before minecraft classes start loading and mixins start colliding and crashing.
        if (ModuleConfig.OCCLUSION_TWEAKS)
            OcclusionCompat.ArchaicFixCompat.crashIfUnsupportedConfigsAreActive();

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
