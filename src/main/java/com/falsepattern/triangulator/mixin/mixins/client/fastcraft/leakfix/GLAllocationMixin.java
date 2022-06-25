package com.falsepattern.triangulator.mixin.mixins.client.fastcraft.leakfix;

import com.falsepattern.triangulator.Triangulator;
import com.falsepattern.triangulator.mixin.helper.LeakFix;
import lombok.val;
import net.minecraft.client.renderer.GLAllocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.Map;

@Mixin(GLAllocation.class)
public abstract class GLAllocationMixin {
    @Shadow @Final private static Map<Integer, Integer> mapDisplayLists;

    @Inject(method = "deleteDisplayLists",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private static void nullSafeDealloc(int p_74523_0_, CallbackInfo ci) {
        if (LeakFix.ENABLED && !mapDisplayLists.containsKey(p_74523_0_)) {
            ci.cancel();
        }
    }

    @Inject(method = "generateDisplayLists",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private static void callerSensitiveAlloc(int p_74526_0_, CallbackInfoReturnable<Integer> cir) {
        if (LeakFix.ENABLED) {
            val trace = Thread.currentThread().getStackTrace()[3];
            if (trace.getMethodName().equals("a") && trace.getClassName().equals("fastcraft.ak")) {
                //Line 336 in FastCraft 1.23
                //Line 53 in FastCraft 1.25
                //^ If anything breaks, check if the caller points are precisely these
                cir.setReturnValue(-1);
            }
        }
    }
}
