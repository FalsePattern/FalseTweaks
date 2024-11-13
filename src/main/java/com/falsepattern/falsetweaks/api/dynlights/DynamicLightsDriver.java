package com.falsepattern.falsetweaks.api.dynlights;

import com.falsepattern.lib.StableAPI;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

@StableAPI(since = "__EXPERIMENTAL__")
public interface DynamicLightsDriver {
    @StableAPI.Expose
    DynamicLightsDriver forWorldMesh();
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
    void removeLights(RenderGlobal renderGlobal);
    @StableAPI.Expose
    void clear();
    @StableAPI.Expose
    int getCount();
}
