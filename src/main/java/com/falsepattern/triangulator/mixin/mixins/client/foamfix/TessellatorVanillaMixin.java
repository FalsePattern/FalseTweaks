package com.falsepattern.triangulator.mixin.mixins.client.foamfix;

import com.falsepattern.triangulator.mixin.helper.ITessellatorMixin;
import net.minecraft.client.renderer.Tessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.util.Comparator;

@Mixin(Tessellator.class)
public abstract class TessellatorVanillaMixin implements ITessellatorMixin {
    @ModifyArg(method = {"getVertexState"},
               at = @At(value = "INVOKE",
                        target = "Ljava/util/PriorityQueue;<init>(ILjava/util/Comparator;)V",
                        remap = false),
               index = 1,
               require = 1)
    private Comparator<?> hackQuadComparator_MIXIN(Comparator<?> comparator) {
        return hackQuadComparator(comparator);
    }

    //Intvalue 72 is for optifine compat
    @ModifyConstant(method = "getVertexState",
                    constant = {@Constant(intValue = 32), @Constant(intValue = 72)},
                    require = 1)
    private int hackQuadCounting_MIXIN(int constant) {
        return hackQuadCounting(constant);
    }
}
