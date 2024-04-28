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

package com.falsepattern.falsetweaks.modules.renderlists;

import com.falsepattern.falsetweaks.Share;
import com.falsepattern.falsetweaks.config.RenderListConfig;
import com.falsepattern.falsetweaks.modules.voxelizer.VoxelMesh;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VoxelRenderListManager implements IResourceManagerReloadListener {
    public static final VoxelRenderListManager INSTANCE = new VoxelRenderListManager();

    private final Map<String, Integer> theMap = new HashMap<>();
    private final List<String> identityList = new ArrayList<>();
    private int list = 0;

    public boolean pre(VoxelMesh mesh, int overlayLayer, boolean remapUV) {
        val identity = mesh.getIdentity(overlayLayer, remapUV);
        if (theMap.containsKey(identity)) {
            val list = theMap.get(identity);
            identityList.add(identityList.remove(identityList.indexOf(identity)));
            GL11.glCallList(list);
            return true;
        } else {
            if (identityList.size() >= RenderListConfig.MAX_BUFFER_SIZE) {
                val oldIden = identityList.remove(0);
                GLAllocation.deleteDisplayLists(theMap.remove(oldIden));
            }
            list = GLAllocation.generateDisplayLists(1);
            identityList.add(identity);
            theMap.put(identity, list);
            GL11.glNewList(list, GL11.GL_COMPILE);
            return false;
        }
    }

    public void post() {
        GL11.glEndList();
        GL11.glCallList(list);
    }

    @Override
    public void onResourceManagerReload(IResourceManager p_110549_1_) {
        Share.log.info("Resource pack reloaded! Clearing voxel render list cache.");
        identityList.clear();
        theMap.forEach((key, value) -> GLAllocation.deleteDisplayLists(value));
        theMap.clear();
    }
}
