package com.falsepattern.falsetweaks.modules.dynlights.base;

import com.falsepattern.falsetweaks.api.dynlights.FTDynamicLights;
import com.falsepattern.falsetweaks.modules.dynlights.BlockPosUtil;
import com.falsepattern.falsetweaks.modules.dynlights.DynamicLightsDrivers;
import com.falsepattern.lib.util.MathUtil;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import lombok.Getter;
import lombok.val;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class DynamicLight {
    @Getter
    private Entity entity = null;
    @Getter
    private double offsetY = 0.0;
    @Getter
    private double lastPosX = -2.14748365E9F;
    @Getter
    private double lastPosY = -2.14748365E9F;
    @Getter
    private double lastPosZ = -2.14748365E9F;
    @Getter
    private int lastLightLevel = 0;
    @Getter
    private boolean underwater = false;
    private long timeCheckMs = 0L;
    private LongSet setLitChunkPos = new LongOpenHashSet();

    public DynamicLight(Entity entity) {
        this.entity = entity;
        this.offsetY = (double)entity.getEyeHeight();
    }

    public void update(RenderGlobal renderGlobal) {
        if (DynamicLightsDrivers.isDynamicLightsFast()) {
            long timeNowMs = System.currentTimeMillis();
            if (timeNowMs < this.timeCheckMs + 500L) {
                return;
            }

            this.timeCheckMs = timeNowMs;
        }

        double posX = this.entity.posX - 0.5;
        double posY = this.entity.posY - 0.5 + this.offsetY;
        double posZ = this.entity.posZ - 0.5;
        int lightLevel = FTDynamicLights.frontend().getLightLevel(this.entity);
        double dx = posX - this.lastPosX;
        double dy = posY - this.lastPosY;
        double dz = posZ - this.lastPosZ;
        double delta = 0.1;
        if (!(Math.abs(dx) <= delta) || !(Math.abs(dy) <= delta) || !(Math.abs(dz) <= delta) || this.lastLightLevel != lightLevel) {
            this.lastPosX = posX;
            this.lastPosY = posY;
            this.lastPosZ = posZ;
            this.lastLightLevel = lightLevel;
            this.underwater = false;
            World world = renderGlobal.theWorld;
            if (world != null) {
                Block block = world.getBlock(MathUtil.floor(posX), MathUtil.floor(posY), MathUtil.floor(posZ));
                this.underwater = block == Blocks.water;
            }

            LongSet setNewPos = new LongOpenHashSet();
            if (lightLevel > 0) {
                EnumFacing dirX = (MathUtil.floor(posX) & 15) >= 8 ? EnumFacing.EAST : EnumFacing.WEST;
                EnumFacing dirY = (MathUtil.floor(posY) & 15) >= 8 ? EnumFacing.UP : EnumFacing.DOWN;
                EnumFacing dirZ = (MathUtil.floor(posZ) & 15) >= 8 ? EnumFacing.SOUTH : EnumFacing.NORTH;
                dirX = this.getOpposite(dirX);
                val bX = MathUtil.floor(posX);
                val bY = MathUtil.floor(posY);
                val bZ = MathUtil.floor(posZ);
                val cX = MathUtil.intFloorDiv(bX, 16) * 16;
                val cY = MathUtil.intFloorDiv(bY, 16) * 16;
                val cZ = MathUtil.intFloorDiv(bZ, 16) * 16;
                val chunkView = BlockPosUtil.packToLong(cX, cY, cZ);
                long chunkX = getRenderChunk(cX, cY, cZ, dirX);
                long chunkZ = getRenderChunk(cX, cY, cZ, dirZ);
                long chunkXZ = getRenderChunk(chunkX, dirZ);
                long chunkY = getRenderChunk(cX, cY, cZ, dirY);
                long chunkYX = getRenderChunk(chunkY, dirX);
                long chunkYZ = getRenderChunk(chunkY, dirZ);
                long chunkYXZ = getRenderChunk(chunkYX, dirZ);
                this.updateChunkLight(renderGlobal, chunkView, this.setLitChunkPos, setNewPos);
                this.updateChunkLight(renderGlobal, chunkX, this.setLitChunkPos, setNewPos);
                this.updateChunkLight(renderGlobal, chunkZ, this.setLitChunkPos, setNewPos);
                this.updateChunkLight(renderGlobal, chunkXZ, this.setLitChunkPos, setNewPos);
                this.updateChunkLight(renderGlobal, chunkY, this.setLitChunkPos, setNewPos);
                this.updateChunkLight(renderGlobal, chunkYX, this.setLitChunkPos, setNewPos);
                this.updateChunkLight(renderGlobal, chunkYZ, this.setLitChunkPos, setNewPos);
                this.updateChunkLight(renderGlobal, chunkYXZ, this.setLitChunkPos, setNewPos);
            }

            this.updateLitChunks(renderGlobal);
            this.setLitChunkPos = setNewPos;
        }
    }

    private EnumFacing getOpposite(EnumFacing facing) {
        switch(facing) {
            case DOWN:
                return EnumFacing.UP;
            case UP:
                return EnumFacing.DOWN;
            case NORTH:
                return EnumFacing.SOUTH;
            case SOUTH:
                return EnumFacing.NORTH;
            case EAST:
                return EnumFacing.WEST;
            case WEST:
                return EnumFacing.EAST;
            default:
                return EnumFacing.DOWN;
        }
    }

    private static long getRenderChunk(long pos, EnumFacing facing) {
        val x = BlockPosUtil.getX(pos);
        val y = BlockPosUtil.getY(pos);
        val z = BlockPosUtil.getZ(pos);
        return getRenderChunk(x, y, z, facing);
    }

    private static long getRenderChunk(int x, int y, int z, EnumFacing facing) {
        return BlockPosUtil.packToLong(x + facing.getFrontOffsetX() * 16, y + facing.getFrontOffsetY() * 16, z + facing.getFrontOffsetZ() * 16);
    }

    private void updateChunkLight(RenderGlobal renderGlobal, long pos, LongSet setPrevPos, LongSet setNewPos) {
        val x = BlockPosUtil.getX(pos);
        val y = BlockPosUtil.getY(pos);
        val z = BlockPosUtil.getZ(pos);
        renderGlobal.markBlockForUpdate(x + 8, y + 8, z + 8);
        if (setPrevPos != null) {
            setPrevPos.remove(pos);
        }

        if (setNewPos != null) {
            setNewPos.add(pos);
        }
    }

    public void updateLitChunks(RenderGlobal renderGlobal) {
        for(long posOld : this.setLitChunkPos) {
            this.updateChunkLight(renderGlobal, posOld, null, null);
        }
    }

    public String toString() {
        return "Entity: " + this.entity + ", offsetY: " + this.offsetY;
    }
}
