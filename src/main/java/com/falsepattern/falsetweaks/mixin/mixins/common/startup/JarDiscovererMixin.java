/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2025 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalseTweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
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
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import cpw.mods.fml.common.discovery.JarDiscoverer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(value = JarDiscoverer.class,
       remap = false)
public abstract class JarDiscovererMixin {
    @Redirect(method = "discover",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/regex/Pattern;matcher(Ljava/lang/CharSequence;)Ljava/util/regex/Matcher;"),
              require = 1)
    private Matcher noMatcher(Pattern instance,
                              CharSequence charSequence,
                              @Share("fileName") LocalRef<String> fileName) {
        fileName.set(charSequence.toString());
        return null;
    }

    @Redirect(method = "discover",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/regex/Matcher;matches()Z"),
              require = 1)
    private boolean fastMatch(Matcher instance, @Share("fileName") LocalRef<String> fileName) {
        return RegexHelper.classFileRegex(fileName.get());
    }
}
