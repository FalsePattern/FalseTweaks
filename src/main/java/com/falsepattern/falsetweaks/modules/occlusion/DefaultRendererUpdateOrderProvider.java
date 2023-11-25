package com.falsepattern.falsetweaks.modules.occlusion;

import gnu.trove.list.array.TIntArrayList;
import lombok.val;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.EnumFacing;

import java.util.List;

public class DefaultRendererUpdateOrderProvider implements IRendererUpdateOrderProvider {

    private final TIntArrayList indices = new TIntArrayList();
    private int lastUpdatedIndex = 0;
    private WorldRenderer nextSample;

    @Override
    public void prepare(List<WorldRenderer> worldRenderersToUpdateList, int updateLimit) {
        lastUpdatedIndex = 0;
        indices.clear();
        nextSample = null;
    }

    @Override
    public boolean hasNext(List<WorldRenderer> worldRenderersToUpdateList) {
        if (nextSample != null)
            return true;
        nextSample = pollNext(worldRenderersToUpdateList);
        return nextSample != null;
    }

    @Override
    public WorldRenderer next(List<WorldRenderer> worldRenderersToUpdateList) {
        if (nextSample != null) {
            val next = nextSample;
            nextSample = null;
            return next;
        }
        return pollNext(worldRenderersToUpdateList);
    }

    private WorldRenderer pollNext(List<WorldRenderer> worldRenderersToUpdateList) {
        main:
        while (lastUpdatedIndex < worldRenderersToUpdateList.size()) {
            val index = lastUpdatedIndex++;
            val wr = worldRenderersToUpdateList.get(index);
            val ci = ((IWorldRenderer)wr).ft$getCullInfo();
            if (ci.visGraph == OcclusionWorker.DUMMY)
                continue;
            for (val facing: OcclusionHelpers.FACING_VALUES) {
                val neighbor = ci.neighbors[facing.ordinal()];
                if (neighbor == null) {
                    if ((facing == EnumFacing.DOWN && wr.posY == 0) ||
                        (facing == EnumFacing.UP && wr.posY == 240)) {
                        continue;
                    } else {
                        continue main;
                    }
                }
                if (neighbor.visGraph == OcclusionWorker.DUMMY)
                    continue main;
            }
            indices.add(index);
            return wr;
        }
        return null;
    }

    @Override
    public void cleanup(List<WorldRenderer> worldRenderersToUpdateList) {
        for (int i = indices.size() - 1; i >= 0; i--) {
            worldRenderersToUpdateList.remove(indices.get(i));
        }
        indices.clear();
    }

}