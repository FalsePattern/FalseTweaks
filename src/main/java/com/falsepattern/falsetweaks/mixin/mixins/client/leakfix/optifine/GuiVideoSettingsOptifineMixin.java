/*
 * FalseTweaks
 *
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice, this permission notice and the word "SNEED"
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.falsetweaks.mixin.mixins.client.leakfix.optifine;

import lombok.SneakyThrows;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.settings.GameSettings;
import cpw.mods.fml.relauncher.ReflectionHelper;

import java.util.List;

@Mixin(GuiVideoSettings.class)
public abstract class GuiVideoSettingsOptifineMixin {
    @SuppressWarnings({"rawtypes", "unchecked"})
    @SneakyThrows
    @Redirect(method = "initGui",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/List;add(Ljava/lang/Object;)Z"),
              require = 2)
    private boolean hackAdd(List instance, Object e) {
        if (e.getClass().getName().equals("GuiOptionButtonOF")) {
            val b = (GuiOptionButton) e;
            val field = ReflectionHelper.findField(b.getClass(), "option");
            val option = (GameSettings.Options) field.get(b);
            if (option.getEnumString().equals("Chunk Loading")) {
                b.enabled = false;
            }
        }
        return instance.add(e);
    }
}
