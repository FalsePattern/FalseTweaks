package com.falsepattern.falsetweaks.mixin.mixins.client.dynlights.of;

import com.falsepattern.falsetweaks.modules.dynlights.DynamicLightsDrivers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

@Mixin(targets = "DynamicLights",
       remap = false)
public abstract class DynamicLightsMixin {

    /**
     * @author FalsePattern
     * @reason Integration
     */
    @Overwrite
    public static void entityAdded(Entity entityIn, RenderGlobal renderGlobal) {
        DynamicLightsDrivers.backend.entityAdded(entityIn, renderGlobal);
    }

    /**
     * @author FalsePattern
     * @reason Integration
     */
    @Overwrite
    public static void entityRemoved(Entity entityIn, RenderGlobal renderGlobal) {
        DynamicLightsDrivers.backend.entityRemoved(entityIn, renderGlobal);
    }

    /**
     * @author FalsePattern
     * @reason Integration
     */
    @Overwrite
    public static void update(RenderGlobal renderGlobal) {
        DynamicLightsDrivers.backend.update(renderGlobal);
    }

    /**
     * @author FalsePattern
     * @reason Integration
     */
    @Overwrite
    public static int getCombinedLight(int x, int y, int z, int combinedLight) {
        return DynamicLightsDrivers.backend.getCombinedLight(x, y, z, combinedLight);
    }

    /**
     * @author FalsePattern
     * @reason Integration
     */
    @Overwrite
    public static int getCombinedLight(Entity entity, int combinedLight) {
        return DynamicLightsDrivers.backend.getCombinedLight(entity, combinedLight);
    }

    /**
     * @author FalsePattern
     * @reason Integration
     */
    @Overwrite
    public static int getCombinedLight(double lightPlayer, int combinedLight) {
        return DynamicLightsDrivers.backend.getCombinedLight(lightPlayer, combinedLight);
    }

    /**
     * @author FalsePattern
     * @reason Integration
     */
    @Overwrite
    public static double getLightLevel(int x, int y, int z) {
        return DynamicLightsDrivers.backend.getLightLevel(x, y, z);
    }

    /**
     * @author FalsePattern
     * @reason Integration
     */
    @Overwrite
    public static int getLightLevel(ItemStack itemStack) {
        return DynamicLightsDrivers.backend.getLightLevel(itemStack);
    }

    /**
     * @author FalsePattern
     * @reason Integration
     */
    @Overwrite
    public static int getLightLevel(Entity entity) {
        return DynamicLightsDrivers.backend.getLightLevel(entity);
    }

    /**
     * @author FalsePattern
     * @reason Integration
     */
    @Overwrite
    public static void removeLights(RenderGlobal renderGlobal) {
        DynamicLightsDrivers.backend.removeLights(renderGlobal);
    }

    /**
     * @author FalsePattern
     * @reason Integration
     */
    @Overwrite
    public static void clear() {
        DynamicLightsDrivers.backend.clear();
    }

    /**
     * @author FalsePattern
     * @reason Integration
     */
    @Overwrite
    public static int getCount() {
        DynamicLightsDrivers.backend.clear();
        return DynamicLightsDrivers.backend.getCount();
    }

    /**
     * @author FalsePattern
     * @reason Integration
     */
    @Overwrite
    public static ItemStack getItemStack(EntityItem entityItem) {
        return DynamicLightsDrivers.backend.getItemStack(entityItem);
    }
}
