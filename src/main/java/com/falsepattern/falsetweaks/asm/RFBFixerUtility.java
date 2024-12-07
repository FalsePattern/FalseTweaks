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

package com.falsepattern.falsetweaks.asm;

import com.gtnewhorizons.retrofuturabootstrap.SharedConfig;
import com.gtnewhorizons.retrofuturabootstrap.api.RfbClassTransformerHandle;
import lombok.SneakyThrows;
import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class RFBFixerUtility {
    @SneakyThrows
    public static void removeGTNHLibHook() {
        val theField = SharedConfig.class.getDeclaredField("rfbTransformers");
        theField.setAccessible(true);
        val ref = (AtomicReference<RfbClassTransformerHandle[]>) theField.get(null);
        val arr = new ArrayList<>(Arrays.asList(ref.get()));
        val iter = arr.iterator();
        while (iter.hasNext()) {
            val elem = iter.next();
            if (elem.id().equals("gtnhlib:redirector")) {
                iter.remove();
            }
        }
        ref.set(arr.toArray(new RfbClassTransformerHandle[0]));
    }
}
