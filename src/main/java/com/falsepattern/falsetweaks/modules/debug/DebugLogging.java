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

package com.falsepattern.falsetweaks.modules.debug;

import com.falsepattern.falsetweaks.Share;
import com.falsepattern.falsetweaks.modules.threading.MainThreadContainer;
import lombok.val;
import mega.trace.service.MEGATraceService;

import java.util.function.Supplier;

public class DebugLogging {
    public static void debugLog(Supplier<String> msg) {
        if (!(Debug.ENABLED && (Debug.fineLogJava || Debug.fineLogJavaTrace || Debug.fineLogMegaTrace))) {
            return;
        }
        val msgVal = msg.get();

        if (Debug.fineLogJava || Debug.fineLogJavaTrace) {
            if (Debug.fineLogJavaTrace && MainThreadContainer.isMainThread()) {
                Share.log.info(msgVal, new Throwable());
            } else {
                Share.log.info(msgVal);
            }
        }

        if (Debug.fineLogMegaTrace) {
            MEGATraceService.INSTANCE.message(msgVal);
        }
    }
}
