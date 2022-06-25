package com.falsepattern.triangulator.mixin.mixins.client.vanilla.leakfix;

import com.falsepattern.triangulator.mixin.helper.LeakFix;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public abstract class RenderGlobalMixin {

    @Redirect(method = "<init>",
              slice = @Slice(from = @At(value = "INVOKE",
                                        target = "Lnet/minecraft/client/Minecraft;getTextureManager()Lnet/minecraft/client/renderer/texture/TextureManager;")),
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/GLAllocation;generateDisplayLists(I)I",
                       ordinal = 0),
              require = 1)
    private int removeCreate(int p_74526_0_) {
        if (!LeakFix.ENABLED) {
            return GLAllocation.generateDisplayLists(p_74526_0_);
        } else {
            return -1;
        }
    }

    @Redirect(method = "deleteAllDisplayLists",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/GLAllocation;deleteDisplayLists(I)V"),
            require = 1)
    private void removeDelete(int list) {
        if (!LeakFix.ENABLED) {
            GLAllocation.deleteDisplayLists(list);
        }
    }
}
