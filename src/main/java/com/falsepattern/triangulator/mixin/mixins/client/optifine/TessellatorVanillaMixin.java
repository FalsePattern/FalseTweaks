package com.falsepattern.triangulator.mixin.mixins.client.optifine;

import com.falsepattern.triangulator.mixin.helper.ITessellatorMixin;
import lombok.val;
import net.minecraft.client.renderer.Tessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Tessellator.class)
public abstract class TessellatorVanillaMixin implements ITessellatorMixin {
    @Redirect(method = "draw",
              at = @At(value = "INVOKE",
                       target = "Ljava/lang/Math;min(II)I",
                       ordinal = 0),
              require = 1)
    private int snapTo3(int a, int b) {
        val v = Math.min(a, b);
        if (isDrawingTris()) {
            return v - (v % 3);
        } else {
            return v;
        }
    }
}
