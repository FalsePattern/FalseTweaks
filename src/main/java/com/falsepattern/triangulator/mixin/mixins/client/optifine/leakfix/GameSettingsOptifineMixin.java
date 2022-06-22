package com.falsepattern.triangulator.mixin.mixins.client.optifine.leakfix;

import com.falsepattern.triangulator.LeakFixState;
import com.falsepattern.triangulator.TriConfig;
import com.falsepattern.triangulator.mixin.helper.LeakFix;
import net.minecraft.client.settings.GameSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
@Mixin(GameSettings.class)
public abstract class GameSettingsOptifineMixin {
    @Shadow(remap = false) public int ofChunkLoading;

    @Inject(method = "setOptionValue",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/settings/GameSettings;updateChunkLoading()V",
                     shift = At.Shift.BEFORE,
                     remap = false),
            require = 1)
    private void lockToStandardChunkLoading(CallbackInfo ci) {
        if (LeakFix.ENABLED) {
            ofChunkLoading = 0;
        }
    }

    @Inject(method = "loadOfOptions",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/settings/GameSettings;updateChunkLoading()V",
                     shift = At.Shift.BEFORE,
                     remap = false),
            require = 1,
            remap = false)
    private void resetToStandardUnchecked(CallbackInfo ci) {
        if (!TriConfig.MEMORY_LEAK_FIX.equals(LeakFixState.Disable)) {
            ofChunkLoading = 0;
        }
    }
}
