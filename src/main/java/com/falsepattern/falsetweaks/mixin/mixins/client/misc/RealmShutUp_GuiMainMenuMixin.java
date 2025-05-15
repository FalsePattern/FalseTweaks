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
package com.falsepattern.falsetweaks.mixin.mixins.client.misc;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;

import java.util.List;

@Mixin(GuiMainMenu.class)
public abstract class RealmShutUp_GuiMainMenuMixin {
    @Redirect(method = "addSingleplayerMultiplayerButtons",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                       ordinal = 0),
              slice = @Slice(from = @At(value = "CONSTANT",
                                        args = "stringValue=menu.online")),
              require = 1)
    private boolean noRealmsButton(List instance, Object e) {
        return false;
    }

    @Redirect(method = "addSingleplayerMultiplayerButtons",
              at = @At(value = "FIELD",
                       opcode = Opcodes.PUTFIELD,
                       target = "Lnet/minecraft/client/gui/GuiButton;width:I"),
              require = 2)
    private void dontChangeWidth(GuiButton instance, int value) {

    }

    @Redirect(method = "addSingleplayerMultiplayerButtons",
              at = @At(value = "FIELD",
                       opcode = Opcodes.PUTFIELD,
                       target = "Lnet/minecraft/client/gui/GuiButton;xPosition:I"),
              require = 1)
    private void dontChangeXPosition(GuiButton instance, int value) {

    }
}
