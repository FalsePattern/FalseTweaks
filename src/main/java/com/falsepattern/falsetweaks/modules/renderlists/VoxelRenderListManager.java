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

package com.falsepattern.falsetweaks.modules.renderlists;

import com.falsepattern.falsetweaks.Share;
import com.falsepattern.falsetweaks.config.RenderListConfig;
import com.falsepattern.falsetweaks.modules.voxelizer.VoxelMesh;
import com.falsepattern.falsetweaks.modules.voxelizer.VoxelMeshIdentity;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VoxelRenderListManager implements IResourceManagerReloadListener {
    public static final VoxelRenderListManager INSTANCE = new VoxelRenderListManager();

    private final Object2IntMap<VoxelMeshIdentity> theMap = new Object2IntOpenHashMap<>(RenderListConfig.MAX_BUFFER_SIZE, 0.25f);
    private final ObjectList<VoxelMeshIdentity> identityList = new ObjectArrayList<>();
    private int list = 0;

    {
        theMap.defaultReturnValue(0);
    }

    public boolean pre(VoxelMesh mesh, int overlayLayer, boolean remapUV) {
        val map = theMap;
        val identity = mesh.getIdentity(overlayLayer, remapUV);
        val _list = map.getInt(identity);
        if (_list != 0) {
            lru(identity);
            GL11.glCallList(_list);
            return true;
        } else {
            val idList = identityList;
            if (idList.size() >= RenderListConfig.MAX_BUFFER_SIZE) {
                val oldIden = idList.remove(0);
                val deleteId = map.removeInt(oldIden);
                if (deleteId != 0) {
                    GLAllocation.deleteDisplayLists(deleteId);
                }
            }
            val newList = GLAllocation.generateDisplayLists(1);
            list = newList;
            idList.add(identity);
            map.put(identity, newList);
            GL11.glNewList(newList, GL11.GL_COMPILE);
            return false;
        }
    }

    private void lru(VoxelMeshIdentity identity) {
        identityList.remove(identity);
        identityList.add(identity);
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
