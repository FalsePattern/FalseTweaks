package com.falsepattern.falsetweaks.api.dynlights;

import com.falsepattern.lib.StableAPI;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

@StableAPI(since = "__EXPERIMENTAL__")
public interface DynamicLightsDriver {
    @StableAPI.Expose
    boolean enabled();
    @StableAPI.Expose
    void entityAdded(Entity entityIn, RenderGlobal renderGlobal);
    @StableAPI.Expose
    void entityRemoved(Entity entityIn, RenderGlobal renderGlobal);
    @StableAPI.Expose
    void update(RenderGlobal renderGlobal);
    @StableAPI.Expose
    int getCombinedLight(int x, int y, int z, int combinedLight);
    @StableAPI.Expose
    int getCombinedLight(Entity entity, int combinedLight);
    @StableAPI.Expose
    int getCombinedLight(double lightPlayer, int combinedLight);
    @StableAPI.Expose
    double getLightLevel(int x, int y, int z);
    @StableAPI.Expose
    int getLightLevel(ItemStack itemStack);
    @StableAPI.Expose
    int getLightLevel(Entity entity);
    @StableAPI.Expose
    void removeLights(RenderGlobal renderGlobal);
    @StableAPI.Expose
    void clear();
    @StableAPI.Expose
    int getCount();
    @StableAPI.Expose
    ItemStack getItemStack(EntityItem entityItem);
}
