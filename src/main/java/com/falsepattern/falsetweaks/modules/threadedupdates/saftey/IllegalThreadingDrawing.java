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
package com.falsepattern.falsetweaks.modules.threadedupdates.saftey;

import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;

public class IllegalThreadingDrawing {
    private static final Logger LOG = LogManager.getLogger("THREADING INCOMPATIBILITY");
    private static final HashSet<String> BAD_CLASSES = new HashSet<>();

    public static void logIllegalMan(String modName, String modid) {
        val e = new UnsupportedOperationException();
        val trace = e.getStackTrace();
        val callerMethodTrace = trace[4];
        val callerClassName = callerMethodTrace.getClassName();
        if (!BAD_CLASSES.add(callerClassName)) {
            return;
        }

        loudWarning(modName, modid, callerClassName);
        LOG.warn("", e);
        loudWarning(modName, modid, callerClassName);
    }

    //loud warning to make sure user notices
    private static void loudWarning(String modName, String modid, String callerClassName) {
        for (int i = 0; i < 10; i++) {
            LOG.fatal(
                    "Mod: [{}] with id: [{}] is not using threaded tess in class: [{}]. Please add it to the falsetweaks.cfg file, TESSELLATOR_USE_REPLACEMENT_TARGETS list!",
                    modName,
                    modid,
                    callerClassName);
        }
    }
}
