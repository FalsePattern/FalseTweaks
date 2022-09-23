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

package com.falsepattern.falsetweaks;

import com.falsepattern.lib.dependencies.DependencyLoader;
import com.falsepattern.lib.dependencies.Library;
import com.falsepattern.lib.dependencies.SemanticVersion;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

public class CoreLoadingPlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
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
        DependencyLoader.addMavenRepo("https://repo.maven.apache.org/maven2/");
        DependencyLoader.loadLibraries(Library.builder()
                                              .loadingModId(Tags.MODID)
                                              .groupId("org.joml")
                                              .artifactId("joml")
                                              .minVersion(new SemanticVersion(1, 10, 4))
                                              .maxVersion(new SemanticVersion(1, 10, Integer.MAX_VALUE))
                                              .preferredVersion(new SemanticVersion(1, 10, 5))
                                              .build());
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
