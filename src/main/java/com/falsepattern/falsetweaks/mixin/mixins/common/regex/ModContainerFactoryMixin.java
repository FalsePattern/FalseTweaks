package com.falsepattern.falsetweaks.mixin.mixins.common.regex;

import com.falsepattern.falsetweaks.mixin.helper.RegexHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import cpw.mods.fml.common.ModContainerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(value = ModContainerFactory.class,
       remap = false)
public abstract class ModContainerFactoryMixin {
    private String fileName;

    @Redirect(method = "build",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/regex/Pattern;matcher(Ljava/lang/CharSequence;)Ljava/util/regex/Matcher;"),
              require = 1)
    private Matcher noMatcher(Pattern instance, CharSequence charSequence) {
        fileName = charSequence.toString();
        return null;
    }


    @Redirect(method = "build",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/regex/Matcher;find()Z"),
              require = 1)
    private boolean fastMatch(Matcher instance) {
        return RegexHelper.modClassRegex(fileName);
    }
}
