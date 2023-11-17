package com.falsepattern.falsetweaks.modules.occlusion;

import net.minecraft.world.chunk.Chunk;

public interface ICulledChunk {
    VisGraph[] getVisibility();
    Chunk buildCulledSides();
}
