package com.falsepattern.falsetweaks.mixin.mixins.common.regex;

import com.falsepattern.falsetweaks.mixin.helper.RegexHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import cpw.mods.fml.common.discovery.ModDiscoverer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(value = ModDiscoverer.class,
       remap = false)
public abstract class ModDiscovererMixin {
    private String fileName;

    @Redirect(method = "findModDirMods(Ljava/io/File;[Ljava/io/File;)V",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/regex/Pattern;matcher(Ljava/lang/CharSequence;)Ljava/util/regex/Matcher;"),
              require = 1)
    private Matcher noMatcher(Pattern instance, CharSequence charSequence) {
        fileName = charSequence.toString();
        return null;
    }

    @Redirect(method = "findModDirMods(Ljava/io/File;[Ljava/io/File;)V",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/regex/Matcher;matches()Z"),
              require = 1)
    private boolean fastMatch(Matcher instance) {
        return RegexHelper.zipJarRegex(fileName);
    }

    @Redirect(method = "findModDirMods(Ljava/io/File;[Ljava/io/File;)V",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/regex/Matcher;group(I)Ljava/lang/String;"),
              require = 1)
    private String noGroup(Matcher instance, int i) {
        return fileName;
    }
}
