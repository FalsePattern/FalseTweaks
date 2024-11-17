package com.falsepattern.falsetweaks.mixin.mixins.client.optispam;

import com.falsepattern.falsetweaks.config.OptiSpamConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import shadersmod.client.BlockAliases;
import stubpackage.Config;

@Mixin(value = BlockAliases.class,
       remap = false)
public abstract class BlockAliasesMixin {
    @Redirect(method = "loadBlockAliases",
              at = @At(value = "INVOKE",
                       target = "LConfig;warn(Ljava/lang/String;)V"),
              slice = @Slice(from = @At(value = "INVOKE",
                                        target = "Ljava/util/Properties;load(Ljava/io/InputStream;)V"),
                             to = @At(value = "INVOKE",
                                      target = "LConfig;warn(Ljava/lang/String;)V",
                                      ordinal = 3)))
    private static void suppressWarnings(String s) {
        if (!OptiSpamConfig.INVALID_ID) {
            Config.warn(s);
        }
    }
}
