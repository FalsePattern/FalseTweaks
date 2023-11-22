package com.falsepattern.falsetweaks.mixin.mixins.client.occlusion;

import net.minecraft.client.renderer.culling.ClippingHelper;

import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClippingHelper.class)
public abstract class ClippingHelperMixin {
    @Shadow public float[][] frustum;

    /**
     * @author _
     * @reason _
     */
    @Overwrite
    public boolean isBoxInFrustum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        for (int i = 0; i < 6; ++i) {
            val frust = this.frustum[i];
            double fX = frust[0];
            double fY = frust[1];
            double fZ = frust[2];
            double fW = frust[3];
            double FmX = fX * minX;
            double FMX = fX * maxX;
            double FmY = fY * minY;
            double FMY = fY * maxY;
            double FmZ = fZ * minZ;
            double FMZ = fZ * maxZ;

            if (FmX + FmY + FmZ + fW <= 0.0D &&
                FMX + FmY + FmZ + fW <= 0.0D &&
                FmX + FMY + FmZ + fW <= 0.0D &&
                FMX + FMY + FmZ + fW <= 0.0D &&
                FmX + FmY + FMZ + fW <= 0.0D &&
                FMX + FmY + FMZ + fW <= 0.0D &&
                FmX + FMY + FMZ + fW <= 0.0D &&
                FMX + FMY + FMZ + fW <= 0.0D) {
                return false;
            }
        }

        return true;
    }
}
