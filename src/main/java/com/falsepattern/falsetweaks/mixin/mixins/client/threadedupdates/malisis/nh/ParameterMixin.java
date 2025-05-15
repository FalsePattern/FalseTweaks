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
