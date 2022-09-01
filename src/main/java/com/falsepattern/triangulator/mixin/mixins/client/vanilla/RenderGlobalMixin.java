package com.falsepattern.triangulator.mixin.mixins.client.vanilla;

import com.falsepattern.lib.util.MathUtil;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import java.util.Comparator;
import java.util.List;

@Mixin(RenderGlobal.class)
public abstract class RenderGlobalMixin {
    @Shadow public List tileEntities;

    @Inject(method = "renderEntities",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/RenderHelper;enableStandardItemLighting()V",
                     shift = At.Shift.AFTER),
            require = 1)
    private void sortTEs(EntityLivingBase player, ICamera camera, float time, CallbackInfo ci) {
        val TEs = (List<TileEntity>)tileEntities;
        TEs.sort(Comparator.comparingDouble((te) -> -AABBDistance(te.getRenderBoundingBox(), player)));
    }

    private static double AABBDistance(AxisAlignedBB AABB, EntityLivingBase player) {
        val x = player.posX;
        val y = player.posY;
        val z = player.posZ;
        val dx = max(AABB.minX - x, 0, x - AABB.maxX);
        val dy = max(AABB.minY - y, 0, y - AABB.maxY);
        val dz = max(AABB.minZ - z, 0, z - AABB.maxZ);
        return dx * dx + dy * dy + dz * dz;
    }

    private static double max(double a, double b, double c) {
        return Math.max(a, Math.max(b, c));
    }
}
