package com.falsepattern.falsetweaks.mixin.mixins.client.optispam;

import com.falsepattern.falsetweaks.config.OptiSpamConfig;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import shadersmod.client.ShaderOption;
import shadersmod.client.ShaderPackParser;
import shadersmod.common.SMCLog;
import stubpackage.Config;

@Mixin(value = ShaderPackParser.class,
       remap = false)
public abstract class ShaderPackParserMixin {
    @Redirect(method = "collectShaderOptions(Lshadersmod/client/IShaderPack;Ljava/lang/String;Ljava/util/Map;)V",
              at = @At(value = "INVOKE",
                       target = "Lshadersmod/client/ShaderOption;getValueDefault()Ljava/lang/String;",
                       ordinal = 0))
    private static String captureSo2(ShaderOption instance, @Share("captured") LocalRef<ShaderOption> so2) {
        if (OptiSpamConfig.AMBIGUOUS_SHADER_OPTION)
            so2.set(instance);
        return instance.getValueDefault();
    }

    @Redirect(method = "collectShaderOptions(Lshadersmod/client/IShaderPack;Ljava/lang/String;Ljava/util/Map;)V",
              at = @At(value = "INVOKE",
                       target = "LConfig;equals(Ljava/lang/Object;Ljava/lang/Object;)Z"))
    private static boolean hijackEquals(Object o1, Object o2, @Share("captured") LocalRef<ShaderOption> so2) {
        if (!OptiSpamConfig.AMBIGUOUS_SHADER_OPTION) {
            return Config.equals(o1, o2);
        }
        if (!Config.equals(o1, o2)) {
            so2.get().setEnabled(false);
        }
        return true;
    }

    @Redirect(method = "parseCustomUniforms",
              at = @At(value = "INVOKE",
                       target = "Lshadersmod/common/SMCLog;warning(Ljava/lang/String;)V"))
    private static void customUniWarn(String message) {
        if (!OptiSpamConfig.CUSTOM_UNIFORMS) {
            SMCLog.warning(message);
        }
    }

    @Redirect(method = "parseCustomUniforms",
              at = @At(value = "INVOKE",
                       target = "Lshadersmod/common/SMCLog;info(Ljava/lang/String;)V"))
    private static void customUniInfo(String message) {
        if (!OptiSpamConfig.CUSTOM_UNIFORMS) {
            SMCLog.info(message);
        }
    }
}
