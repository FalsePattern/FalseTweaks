package com.falsepattern.falsetweaks.modules.dynlights;

import com.falsepattern.falsetweaks.api.dynlights.DynamicLightsDriver;
import stubpackage.Config;
import stubpackage.DynamicLights;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

public class DynamicLightsOF implements DynamicLightsDriver {
    @Override
    public boolean enabled() {
        return Config.isDynamicLights();
    }

    @Override
    public void entityAdded(Entity entityIn, RenderGlobal renderGlobal) {
        DynamicLights.entityAdded(entityIn, renderGlobal);
    }

    @Override
    public void entityRemoved(Entity entityIn, RenderGlobal renderGlobal) {
        DynamicLights.entityRemoved(entityIn, renderGlobal);
    }

    @Override
    public void update(RenderGlobal renderGlobal) {
        DynamicLights.update(renderGlobal);
    }

    @Override
    public int getCombinedLight(int x, int y, int z, int combinedLight) {
        return DynamicLights.getCombinedLight(x, y, z, combinedLight);
    }

    @Override
    public int getCombinedLight(Entity entity, int combinedLight) {
        return DynamicLights.getCombinedLight(entity, combinedLight);
    }

    @Override
    public int getCombinedLight(double lightPlayer, int combinedLight) {
        return DynamicLights.getCombinedLight(lightPlayer, combinedLight);
    }

    @Override
    public double getLightLevel(int x, int y, int z) {
        return DynamicLights.getLightLevel(x, y, z);
    }

    @Override
    public int getLightLevel(ItemStack itemStack) {
        return DynamicLights.getLightLevel(itemStack);
    }

    @Override
    public int getLightLevel(Entity entity) {
        return DynamicLights.getLightLevel(entity);
    }

    @Override
    public void removeLights(RenderGlobal renderGlobal) {
        DynamicLights.removeLights(renderGlobal);
    }

    @Override
    public void clear() {
        DynamicLights.clear();
    }

    @Override
    public int getCount() {
        return DynamicLights.getCount();
    }

    @Override
    public ItemStack getItemStack(EntityItem entityItem) {
        return DynamicLights.getItemStack(entityItem);
    }
}
