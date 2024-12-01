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

package com.falsepattern.falsetweaks.modules.dynlights.base;

import com.falsepattern.falsetweaks.Compat;
import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.falsetweaks.api.dynlights.DynamicLightsDriver;
import com.falsepattern.falsetweaks.api.dynlights.FTDynamicLights;
import com.falsepattern.falsetweaks.modules.dynlights.DynamicLightsDrivers;
import com.falsepattern.lib.util.MathUtil;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
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
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;

public class DynamicLights implements DynamicLightsDriver {
    public static final DynamicLights INSTANCE = new DynamicLights(false);
    private static final DynamicLights FOR_WORLD = new DynamicLights(true);

    private static final Int2ObjectMap<DynamicLight> mapDynamicLights = new Int2ObjectArrayMap<>(1024);
    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    private static long timeUpdateMs = 0L;
    private static final double MAX_DIST = 16.0;
    private static final double MAX_DIST_SQ = MAX_DIST * MAX_DIST;
    private static final int LIGHT_LEVEL_MAX = 15;
    private static final int LIGHT_LEVEL_FIRE = 15;
    private static final int LIGHT_LEVEL_BLAZE = 10;
    private static final int LIGHT_LEVEL_MAGMA_CUBE = 8;
    private static final int LIGHT_LEVEL_MAGMA_CUBE_CORE = 13;
    private static final int LIGHT_LEVEL_GLOWSTONE_DUST = 8;
    private final boolean forWorld;

    private static ReentrantReadWriteLock.WriteLock busyWaitWriteLock() {
        val lock = rwLock.writeLock();
        while (!lock.tryLock()) {
            Thread.yield();
        }
        return lock;
    }

    private static ReentrantReadWriteLock.ReadLock busyWaitReadLock() {
        val lock = rwLock.readLock();
        while (!lock.tryLock()) {
            Thread.yield();
        }
        return lock;
    }

    private DynamicLights(boolean forWorld) {
        this.forWorld = forWorld;
        if (!forWorld && !Compat.optiFineHasDynamicLights()) {
            FMLCommonHandler.instance().bus().register(this);
        }
    }

    @SubscribeEvent
    public void onReload(ConfigChangedEvent e) {
        if (e.modID.equals(Tags.MOD_ID)) {
            removeLights(Minecraft.getMinecraft().renderGlobal);
        }
    }

    @Override
    public DynamicLightsDriver forWorldMesh() {
        return FOR_WORLD;
    }

    @Override
    public boolean enabled() {
        return DynamicLightsDrivers.isDynamicLights();
    }

    @Override
    public void entityAdded(Entity entityIn, RenderGlobal renderGlobal) {
    }

    @Override
    public void entityRemoved(Entity entityIn, RenderGlobal renderGlobal) {
        val lock = busyWaitWriteLock();
        try {
            DynamicLight dynamicLight = mapDynamicLights.remove(entityIn.getEntityId());
            if (dynamicLight != null) {
                dynamicLight.updateLitChunks(renderGlobal);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void update(RenderGlobal renderGlobal) {
        long timeNowMs = System.currentTimeMillis();
        if (timeNowMs >= timeUpdateMs + 50L) {
            timeUpdateMs = timeNowMs;
            val lock = busyWaitWriteLock();
            try {
                updateMapDynamicLights(renderGlobal);
                if (!mapDynamicLights.isEmpty()) {
                    for (val dynamicLight : mapDynamicLights.values()) {
                        dynamicLight.update(renderGlobal);
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private void updateMapDynamicLights(RenderGlobal renderGlobal) {
        World world = renderGlobal.theWorld;
        if (world != null) {
            for(Entity entity : world.getLoadedEntityList()) {
                int lightLevel = getLightLevel(entity);
                if (lightLevel > 0) {
                    int key = entity.getEntityId();
                    DynamicLight dynamicLight = mapDynamicLights.get(key);
                    if (dynamicLight == null) {
                        dynamicLight = new DynamicLight(entity);
                        mapDynamicLights.put(key, dynamicLight);
                    }
                } else {
                    int key = entity.getEntityId();
                    DynamicLight dynamicLight = mapDynamicLights.remove(key);
                    if (dynamicLight != null) {
                        dynamicLight.updateLitChunks(renderGlobal);
                    }
                }
            }
        }
    }

    @Override
    public int getCombinedLight(int x, int y, int z, int combinedLight) {
        double lightPlayer = getLightLevel(x, y, z);
        return getCombinedLight(lightPlayer, combinedLight);
    }

    @Override
    public int getCombinedLight(Entity entity, int combinedLight) {
        double lightPlayer = (double)getLightLevel(entity);
        return getCombinedLight(lightPlayer, combinedLight);
    }

    private static int getCombinedLight(double lightPlayer, int combinedLight) {
        if (lightPlayer > 0.0) {
            int lightPlayerFF = (int)(lightPlayer * 16.0);
            int lightBlockFF = combinedLight & 0xFF;
            if (lightPlayerFF > lightBlockFF) {
                combinedLight &= -256;
                combinedLight |= lightPlayerFF;
            }
        }

        return combinedLight;
    }

    private double getLightLevel(int x, int y, int z) {
        val mc = Minecraft.getMinecraft();
        val rve = mc.renderViewEntity;
        double lightLevelMax = 0.0;
        val lock = busyWaitReadLock();
        try {
            for (val dynamicLight: mapDynamicLights.values()) {
                if (!DynamicLightsDrivers.isDynamicHandLight(forWorld) && dynamicLight.getEntity() == rve) {
                    continue;
                }
                int dynamicLightLevel = dynamicLight.getLastLightLevel();
                if (dynamicLightLevel > 0) {
                    double px = dynamicLight.getLastPosX();
                    double py = dynamicLight.getLastPosY();
                    double pz = dynamicLight.getLastPosZ();
                    double dx = (double)x - px;
                    double dy = (double)y - py;
                    double dz = (double)z - pz;
                    if (dynamicLight.isUnderwater()) {
                        dynamicLightLevel = MathUtil.clamp(dynamicLightLevel - 2, 0, LIGHT_LEVEL_MAX);
                        dx *= 2.0;
                        dy *= 2.0;
                        dz *= 2.0;
                    }
                    double light;

                    if (FTDynamicLights.isCircular()) {
                        double distSq = dx * dx + dy * dy + dz * dz;
                        if (distSq <= MAX_DIST_SQ) {
                            light = MathUtil.clamp(dynamicLightLevel - MathUtil.sqrt(distSq), 0, LIGHT_LEVEL_MAX);
                        } else {
                            light = 0;
                        }
                    } else {
                        dx = Math.abs(dx);
                        dy = Math.abs(dy);
                        dz = Math.abs(dz);
                        double dist = Math.max(dx - 0.25, 0) + Math.max(dy - 0.25, 0) + Math.max(dz - 0.25, 0);
                        light = MathUtil.clamp(dynamicLightLevel - dist, 0, LIGHT_LEVEL_MAX);
                    }

                    if (light > lightLevelMax) {
                        lightLevelMax = light;
                    }
                }
            }
        } finally {
            lock.unlock();
        }

        return MathUtil.clamp(lightLevelMax, 0.0, 15.0);
    }

    // Note: Public for easier compat with https://github.com/Tesseract4D/OffhandLights, do not refactor.
    public static int getLightLevel(ItemStack itemStack) {
        if (itemStack == null) {
            return 0;
        } else {
            Item item = itemStack.getItem();
            if (item instanceof ItemBlock) {
                ItemBlock itemBlock = (ItemBlock)item;
                Block block = itemBlock.field_150939_a;
                if (block != null) {
                    return block.getLightValue();
                }
            }

            if (item == Items.lava_bucket) {
                return Blocks.lava.getLightValue();
            } else if (item == Items.blaze_rod || item == Items.blaze_powder) {
                return LIGHT_LEVEL_BLAZE;
            } else if (item == Items.glowstone_dust) {
                return LIGHT_LEVEL_GLOWSTONE_DUST;
            } else if (item == Items.magma_cream) {
                return LIGHT_LEVEL_MAGMA_CUBE;
            } else {
                return item == Items.nether_star ? Blocks.beacon.getLightValue() / 2 : 0;
            }
        }
    }

    public int getLightLevel(Entity entity) {
        if (entity == Minecraft.getMinecraft().renderViewEntity && !DynamicLightsDrivers.isDynamicHandLight(forWorld)) {
            return 0;
        } else if (entity.isBurning()) {
            return LIGHT_LEVEL_FIRE;
        } else if (entity instanceof EntityFireball) {
            return LIGHT_LEVEL_FIRE;
        } else if (entity instanceof EntityTNTPrimed) {
            return LIGHT_LEVEL_FIRE;
        } else if (entity instanceof EntityBlaze) {
            EntityBlaze entityBlaze = (EntityBlaze)entity;
            return entityBlaze.func_70845_n() ? LIGHT_LEVEL_FIRE : LIGHT_LEVEL_BLAZE;
        } else if (entity instanceof EntityMagmaCube) {
            EntityMagmaCube emc = (EntityMagmaCube)entity;
            return (double)emc.squishFactor > 0.6 ? LIGHT_LEVEL_MAGMA_CUBE_CORE : LIGHT_LEVEL_MAGMA_CUBE;
        } else {
            if (entity instanceof EntityCreeper) {
                EntityCreeper entityCreeper = (EntityCreeper)entity;
                if (entityCreeper.getCreeperState() > 0) {
                    return LIGHT_LEVEL_FIRE;
                }
            }

            if (entity instanceof EntityLivingBase) {
                EntityLivingBase player = (EntityLivingBase)entity;
                ItemStack stackMain = player.getHeldItem();
                int levelMain = getLightLevel(stackMain);
                ItemStack stackHead = player.getEquipmentInSlot(4);
                int levelHead = getLightLevel(stackHead);
                return Math.max(levelMain, levelHead);
            } else if (entity instanceof EntityItem) {
                EntityItem entityItem = (EntityItem)entity;
                ItemStack itemStack = getItemStack(entityItem);
                return getLightLevel(itemStack);
            } else {
                return 0;
            }
        }
    }

    @Override
    public void removeLights(RenderGlobal renderGlobal) {
        val lock = busyWaitWriteLock();
        try {
            for (val dynamicLight: mapDynamicLights.values()) {
                dynamicLight.updateLitChunks(renderGlobal);
            }

            mapDynamicLights.clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        val lock = busyWaitWriteLock();
        try {
            mapDynamicLights.clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getCount() {
        val lock = busyWaitReadLock();
        try {
            return mapDynamicLights.size();
        } finally {
            lock.unlock();
        }
    }

    private static ItemStack getItemStack(EntityItem entityItem) {
        return entityItem.getDataWatcher().getWatchableObjectItemStack(10);
    }
}
