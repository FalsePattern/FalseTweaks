package com.falsepattern.falsetweaks.mixin.mixins.client.occlusion;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.gui.GuiVideoSettings;

@Mixin(GuiVideoSettings.class)
public class GuiVideoSettingsMixin {
    @Redirect(method = "initGui",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/client/renderer/OpenGlHelper;field_153197_d:Z"),
              require = 0)
    private boolean neverUseAdvancedGl() {
        return false;
    }
}
