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

package com.falsepattern.falsetweaks.mixin.mixins.client.occlusion.optifine;

import com.falsepattern.falsetweaks.modules.occlusion.OcclusionCompat;
import lombok.SneakyThrows;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiOptionSlider;
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
        GameSettings.Options option = null;
        if (e instanceof GuiOptionSlider) {
            option = ((GuiOptionSlider)e).field_146133_q;
        } else if (e instanceof GuiOptionButton) {
            option = ((GuiOptionButton)e).returnEnumOptions();
        }

        if (option == null)
            return instance.add(e);

        switch (option.name()) {
            case "CHUNK_LOADING":
            case "ADVANCED_OPENGL":
                return OcclusionCompat.OptiFineCompat.disableControl(instance, e);
        }
        return instance.add(e);
    }
}
