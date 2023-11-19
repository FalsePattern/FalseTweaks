/*
 * This file is part of FalseTweaks.
 *
 * FalseTweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FalseTweaks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalseTweaks. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.falsetweaks.mixin.mixins.common.startup;

import com.falsepattern.falsetweaks.modules.startup.RegexHelper;
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
