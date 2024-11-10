package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates.gtnhlib;

import com.gtnewhorizon.gtnhlib.client.renderer.TessellatorManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = TessellatorManager.class,
       remap = false)
public abstract class TessellatorManagerMixin {

    /**
     * @author FalsePattern
     * @reason FalseTweaks hijack
     */
    @Overwrite
    public static boolean isMainInstance(Object instance) {
        return true;
    }
}
