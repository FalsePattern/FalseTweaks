package com.falsepattern.triangulator.mixin.mixins.client.optifine.leakfix;

import com.falsepattern.triangulator.leakfix.LeakFix;
import com.falsepattern.triangulator.mixin.stubpackage.WrDisplayListAllocator;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.RenderGlobal;

@SuppressWarnings({"InvalidInjectorMethodSignature", "UnresolvedMixinReference", "MixinAnnotationTarget"})
@Mixin(RenderGlobal.class)
public abstract class RenderGlobalOptiFineMixin {
    @Shadow(remap = false)
    public WrDisplayListAllocator displayListAllocator;

    @Redirect(method = "<init>",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/client/renderer/RenderGlobal;displayListAllocator:LWrDisplayListAllocator;",
                       opcode = Opcodes.PUTFIELD,
                       remap = false),
              require = 1)
    private void noAllocator(RenderGlobal rg, WrDisplayListAllocator param) {
        if (!LeakFix.ENABLED) {
            displayListAllocator = param;
        }
    }

    @Redirect(method = "loadRenderers",
              at = @At(value = "INVOKE",
                       target = "LWrDisplayListAllocator;resetAllocatedLists()V",
                       remap = false),
              require = 1)
    private void noReset(WrDisplayListAllocator allocator) {
        if (!LeakFix.ENABLED) {
            allocator.resetAllocatedLists();
        }
    }

    @Redirect(method = "deleteAllDisplayLists",
              at = @At(value = "INVOKE",
                       target = "LWrDisplayListAllocator;deleteDisplayLists()V",
                       remap = false),
              require = 1)
    private void noDelete(WrDisplayListAllocator allocator) {
        if (!LeakFix.ENABLED) {
            allocator.deleteDisplayLists();
        }
    }
}
