package com.falsepattern.triangulator.mixin.mixins.client.optifine;

import cpw.mods.fml.relauncher.ReflectionHelper;
import lombok.SneakyThrows;
import lombok.val;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.settings.GameSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(GuiVideoSettings.class)
public abstract class GuiVideoSettingsOptifineMixin {
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
