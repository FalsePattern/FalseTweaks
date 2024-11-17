package com.falsepattern.falsetweaks.mixin.mixins.client.optispam;

import com.falsepattern.falsetweaks.config.OptiSpamConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import shadersmod.common.SMCLog;
import shadersmod.uniform.ShaderExpressionResolver;

@Mixin(value = ShaderExpressionResolver.class,
       remap = false)
public abstract class ShaderExpressionResolverMixin {
    @Redirect(method = "registerExpression",
              at = @At(value = "INVOKE",
                       target = "Lshadersmod/common/SMCLog;warning(Ljava/lang/String;)V"))
    private void suppressWarn(String message) {
        if (!OptiSpamConfig.CUSTOM_UNIFORMS) {
            SMCLog.warning(message);
        }
    }
}
