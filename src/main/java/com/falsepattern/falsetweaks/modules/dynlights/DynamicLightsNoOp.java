package com.falsepattern.falsetweaks.modules.dynlights;

import com.falsepattern.falsetweaks.api.dynlights.DynamicLightsDriver;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

public class DynamicLightsNoOp implements DynamicLightsDriver {
    public static final DynamicLightsNoOp INSTANCE = new DynamicLightsNoOp();
    private DynamicLightsNoOp() {}

    @Override
    public DynamicLightsDriver forWorldMesh() {
        return INSTANCE;
    }

    @Override
    public boolean enabled() {
        return false;
    }

    @Override
    public void entityAdded(Entity entityIn, RenderGlobal renderGlobal) {

    }

    @Override
    public void entityRemoved(Entity entityIn, RenderGlobal renderGlobal) {

    }

    @Override
    public void update(RenderGlobal renderGlobal) {

    }

    @Override
    public int getCombinedLight(int x, int y, int z, int combinedLight) {
        return combinedLight;
    }

    @Override
    public int getCombinedLight(Entity entity, int combinedLight) {
        return combinedLight;
    }

    @Override
    public void removeLights(RenderGlobal renderGlobal) {

    }

    @Override
    public void clear() {

    }

    @Override
    public int getCount() {
        return 0;
    }
}
