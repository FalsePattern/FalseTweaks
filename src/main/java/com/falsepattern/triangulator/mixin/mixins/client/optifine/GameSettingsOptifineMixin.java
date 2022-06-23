package com.falsepattern.triangulator.mixin.mixins.client.optifine;

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

    @Inject(method = "updateChunkLoading",
            at = @At(value = "HEAD"),
            require = 1)
    private void blockMultiThreadedChunkLoading(CallbackInfo ci) {
        ofChunkLoading = 0;
    }
}
