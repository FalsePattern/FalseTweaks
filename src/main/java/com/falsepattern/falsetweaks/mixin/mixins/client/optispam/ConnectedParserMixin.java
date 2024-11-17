package com.falsepattern.falsetweaks.mixin.mixins.client.optispam;

import com.falsepattern.falsetweaks.config.OptiSpamConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import stubpackage.Config;

@Mixin(targets = "ConnectedParser",
       remap = false)
public abstract class ConnectedParserMixin {
    @Redirect(method = "warn",
              at = @At(value = "INVOKE",
                       target = "LConfig;warn(Ljava/lang/String;)V"))
    private void suppressWarn(String s) {
        if (!OptiSpamConfig.BLOCK_NOT_FOUND) {
            Config.warn(s);
        }
    }
}
