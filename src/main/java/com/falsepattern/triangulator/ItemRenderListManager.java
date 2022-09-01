package com.falsepattern.triangulator;

import com.falsepattern.triangulator.config.TriConfig;
import com.falsepattern.triangulator.mixin.helper.ItemProp;
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
public class ItemRenderListManager implements IResourceManagerReloadListener {
    public static final ItemRenderListManager INSTANCE = new ItemRenderListManager();

    private final Map<ItemProp, Integer> theMap = new HashMap<>();
    private final List<ItemProp> propList = new ArrayList<>();
    private final ItemProp prop = new ItemProp();
    private int list = 0;

    public boolean pre(float a, float b, float c, float d, int e, int f, float g) {
        prop.set(a, b, c, d, e, f, g);
        if (theMap.containsKey(prop)) {
            val list = theMap.get(prop);
            propList.add(propList.remove(propList.indexOf(prop)));
            GL11.glCallList(list);
            return true;
        } else {
            if (propList.size() >= TriConfig.ITEM_RENDERLIST_BUFFER_MAX_SIZE) {
                val oldProp = propList.remove(0);
                GLAllocation.deleteDisplayLists(theMap.remove(oldProp));
            }
            list = GLAllocation.generateDisplayLists(1);
            val newProp = new ItemProp(prop);
            theMap.put(newProp, list);
            propList.add(newProp);
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
        Share.log.info("Resource pack reloaded! Clearing item render list cache.");
        propList.clear();
        theMap.forEach((key, value) -> GLAllocation.deleteDisplayLists(value));
        theMap.clear();
    }
}
