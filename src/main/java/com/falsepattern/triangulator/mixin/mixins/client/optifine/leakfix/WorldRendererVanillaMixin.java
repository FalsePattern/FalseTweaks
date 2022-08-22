package com.falsepattern.triangulator.mixin.mixins.client.optifine.leakfix;

import com.falsepattern.triangulator.leakfix.LeakFix;
import com.falsepattern.triangulator.mixin.helper.IWorldRendererMixin;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.EntityLivingBase;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererVanillaMixin implements IWorldRendererMixin {
    @Shadow
    public abstract void markDirty();

    @Inject(method = "updateRenderer",
            at = @At(value = "FIELD",
                     target = "Lnet/minecraft/client/renderer/WorldRenderer;needsUpdate:Z",
                     opcode = Opcodes.PUTFIELD,
                     shift = At.Shift.AFTER),
            require = 1)
    private void prepareRenderList(EntityLivingBase p_147892_1_, CallbackInfo ci) {
        if (LeakFix.ENABLED) {
            if (genList()) {
                renderAABB();
            }
        }
    }

    @Inject(method = "setPosition",
            at = @At(value = "FIELD",
                     target = "Lnet/minecraft/client/renderer/WorldRenderer;rendererBoundingBox:Lnet/minecraft/util/AxisAlignedBB;",
                     opcode = Opcodes.PUTFIELD,
                     shift = At.Shift.AFTER),
            cancellable = true,
            require = 1)
    private void deferBoundBox(int p_78913_1_, int p_78913_2_, int p_78913_3_, CallbackInfo ci) {
        if (LeakFix.ENABLED) {
            this.markDirty();
            ci.cancel();
        }
    }

    @Inject(method = "callOcclusionQueryList",
            at = @At(value = "HEAD"),
            require = 1)
    private void occlusionQueryEarlyGen(CallbackInfo ci) {
        if (LeakFix.ENABLED) {
            if (genList()) {
                renderAABB();
            }
        }
    }
}
