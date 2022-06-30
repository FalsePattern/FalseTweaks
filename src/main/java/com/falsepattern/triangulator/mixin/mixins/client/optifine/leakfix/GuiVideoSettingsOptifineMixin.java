package com.falsepattern.triangulator.mixin.mixins.client.optifine.leakfix;

import com.falsepattern.triangulator.config.TriConfig;
import com.falsepattern.triangulator.leakfix.LeakFix;
import com.falsepattern.triangulator.leakfix.LeakFixState;
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
    @SneakyThrows
    @Redirect(method = "initGui",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/List;add(Ljava/lang/Object;)Z"),
              require = 2)
    private boolean hackAdd(List instance, Object e) {
        if (TriConfig.MEMORY_LEAK_FIX != LeakFixState.Disable) {
            if (e.getClass().getName().equals("GuiOptionButtonOF")) {
                val b = (GuiOptionButton) e;
                val field = ReflectionHelper.findField(b.getClass(), "option");
                val option = (GameSettings.Options) field.get(b);
                if (option.getEnumString().equals("Chunk Loading")) {
                    b.enabled = false;
                }
            }
        }
        return instance.add(e);
    }
}
