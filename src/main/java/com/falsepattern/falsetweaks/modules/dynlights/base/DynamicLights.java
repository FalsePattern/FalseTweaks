package com.falsepattern.falsetweaks.modules.dynlights.base;

import com.falsepattern.falsetweaks.Compat;
import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.falsetweaks.api.dynlights.DynamicLightsDriver;
import com.falsepattern.falsetweaks.config.DynamicLightsConfig;
import com.falsepattern.falsetweaks.modules.dynlights.DynamicLightsDrivers;
import com.falsepattern.lib.util.MathUtil;

import java.util.List;
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
    private final DynamicLightsMap mapDynamicLights = new DynamicLightsMap();
    private static long timeUpdateMs = 0L;
    private static final double MAX_DIST = 7.5;
    private static final double MAX_DIST_SQ = 56.25;
    private static final int LIGHT_LEVEL_MAX = 15;
    private static final int LIGHT_LEVEL_FIRE = 15;
    private static final int LIGHT_LEVEL_BLAZE = 10;
    private static final int LIGHT_LEVEL_MAGMA_CUBE = 8;
    private static final int LIGHT_LEVEL_MAGMA_CUBE_CORE = 13;
    private static final int LIGHT_LEVEL_GLOWSTONE_DUST = 8;

    public DynamicLights() {
        if (!Compat.optiFineHasDynamicLights()) {
            FMLCommonHandler.instance().bus().register(this);
        }
    }

    public void onReload(ConfigChangedEvent e) {
        if (e.modID.equals(Tags.MOD_ID)) {
            removeLights(Minecraft.getMinecraft().renderGlobal);
        }
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
        synchronized(mapDynamicLights) {
            DynamicLight dynamicLight = mapDynamicLights.remove(entityIn.getEntityId());
            if (dynamicLight != null) {
                dynamicLight.updateLitChunks(renderGlobal);
            }
        }
    }

    @Override
    public void update(RenderGlobal renderGlobal) {
        long timeNowMs = System.currentTimeMillis();
        if (timeNowMs >= timeUpdateMs + 50L) {
            timeUpdateMs = timeNowMs;
            synchronized(mapDynamicLights) {
                updateMapDynamicLights(renderGlobal);
                if (mapDynamicLights.size() > 0) {
                    List<DynamicLight> dynamicLights = mapDynamicLights.valueList();

                    for(int i = 0; i < dynamicLights.size(); ++i) {
                        DynamicLight dynamicLight = (DynamicLight)dynamicLights.get(i);
                        dynamicLight.update(renderGlobal);
                    }
                }
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

    @Override
    public int getCombinedLight(double lightPlayer, int combinedLight) {
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

    @Override
    public double getLightLevel(int x, int y, int z) {
        double lightLevelMax = 0.0;
        synchronized(mapDynamicLights) {
            List<DynamicLight> dynamicLights = mapDynamicLights.valueList();

            for(int i = 0; i < dynamicLights.size(); ++i) {
                DynamicLight dynamicLight = (DynamicLight)dynamicLights.get(i);
                int dynamicLightLevel = dynamicLight.getLastLightLevel();
                if (dynamicLightLevel > 0) {
                    double px = dynamicLight.getLastPosX();
                    double py = dynamicLight.getLastPosY();
                    double pz = dynamicLight.getLastPosZ();
                    double dx = (double)x - px;
                    double dy = (double)y - py;
                    double dz = (double)z - pz;
                    double distSq = dx * dx + dy * dy + dz * dz;
                    if (dynamicLight.isUnderwater()) {
                        dynamicLightLevel = MathUtil.clamp(dynamicLightLevel - 2, 0, LIGHT_LEVEL_MAX);
                        distSq *= 2.0;
                    }

                    if (!(distSq > MAX_DIST_SQ)) {
                        double dist = Math.sqrt(distSq);
                        double light = 1.0 - dist / MAX_DIST;
                        double lightLevel = light * (double)dynamicLightLevel;
                        if (lightLevel > lightLevelMax) {
                            lightLevelMax = lightLevel;
                        }
                    }
                }
            }
        }

        return MathUtil.clamp(lightLevelMax, 0.0, 15.0);
    }

    @Override
    public int getLightLevel(ItemStack itemStack) {
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

    @Override
    public int getLightLevel(Entity entity) {
        if (entity == Minecraft.getMinecraft().renderViewEntity && !DynamicLightsDrivers.isDynamicHandLight()) {
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
        synchronized(mapDynamicLights) {
            List<DynamicLight> dynamicLights = mapDynamicLights.valueList();

            for(int i = 0; i < dynamicLights.size(); ++i) {
                DynamicLight dynamicLight = (DynamicLight)dynamicLights.get(i);
                dynamicLight.updateLitChunks(renderGlobal);
            }

            dynamicLights.clear();
        }
    }

    @Override
    public void clear() {
        synchronized(mapDynamicLights) {
            mapDynamicLights.clear();
        }
    }

    @Override
    public int getCount() {
        synchronized(mapDynamicLights) {
            return mapDynamicLights.size();
        }
    }

    @Override
    public ItemStack getItemStack(EntityItem entityItem) {
        return entityItem.getDataWatcher().getWatchableObjectItemStack(10);
    }
}
