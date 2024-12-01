package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates.malisis.nh;

import lombok.val;
import net.malisis.core.renderer.Parameter;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Parameter.class,
       remap = false)
public abstract class ParameterMixin<T> {
    @Shadow
    private T value;

    @Shadow
    private T defaultValue;

    /**
     * @author FalsePattern
     * @reason Thread safe
     */
    @Dynamic
    @Overwrite
    public T merged(Parameter<T> other) {
        val v = this.value;
        @SuppressWarnings("unchecked")
        val ov = ((ParameterMixin<T>) (Object) other).value;
        if (ov != null) {
            return ov;
        } else if (v != null) {
            return v;
        } else {
            return defaultValue;
        }
    }
}
