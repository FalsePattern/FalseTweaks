package com.falsepattern.falsetweaks.modules.occlusion;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.EntityLivingBase;

import java.util.Comparator;

public class BasicDistanceSorter implements Comparator<WorldRenderer> {
    private final EntityLivingBase renderViewEntity;

    public BasicDistanceSorter(EntityLivingBase renderViewEntity) {
        this.renderViewEntity = renderViewEntity;
    }

    @Override
    public int compare(WorldRenderer wr1, WorldRenderer wr2) {
        return (int)((wr1.distanceToEntitySquared(renderViewEntity) - wr2.distanceToEntitySquared(renderViewEntity)) * 1024D);
    }
}
