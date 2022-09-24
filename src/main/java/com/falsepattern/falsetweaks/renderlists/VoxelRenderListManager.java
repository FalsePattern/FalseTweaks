/*
 * Triangulator
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

package com.falsepattern.falsetweaks.renderlists;

import com.falsepattern.falsetweaks.Share;
import com.falsepattern.falsetweaks.config.FTConfig;
import com.falsepattern.falsetweaks.voxelizer.VoxelMesh;
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

    public boolean pre(VoxelMesh mesh, boolean glint) {
        val identity = mesh.getIdentity(glint);
        if (theMap.containsKey(identity)) {
            val list = theMap.get(identity);
            identityList.add(identityList.remove(identityList.indexOf(identity)));
            GL11.glCallList(list);
            return true;
        } else {
            if (identityList.size() >= FTConfig.ITEM_RENDERLIST_BUFFER_MAX_SIZE) {
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
