package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates.malisis;

import lombok.val;
import net.malisis.core.renderer.Parameter;
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
    @Overwrite
    public T get() {
        val v = this.value;
        return v != null ? v : defaultValue;
    }

    /**
     * @author FalsePattern
     * @reason Thread safe
     */
    @Overwrite
    public Object get(int index) {
        val value = this.value;
        if (value == null) {
            return null;
        }
        if (!(value instanceof Object[])) {
            throw new IllegalStateException("Trying to access indexed element of non-array Parameter");
        }

        Object[] v = (Object[]) value;
        if (index < 0 || index >= v.length) {
            return null;
        }

        return v[index];
    }

    /**
     * @author FalsePattern
     * @reason Thread safe
     */
    @Overwrite
    public void merge(Parameter<T> parameter) {
        val pv = parameter.getValue();
        if (pv != null) {
            value = pv;
        }
    }
}
