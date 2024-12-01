/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2024 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
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
