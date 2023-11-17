package com.falsepattern.falsetweaks.modules.occlusion;

public interface IWorldRenderer {

    boolean ft$isInUpdateList();
    void ft$setInUpdateList(boolean b);

    OcclusionWorker.CullInfo ft$getCullInfo();

}
