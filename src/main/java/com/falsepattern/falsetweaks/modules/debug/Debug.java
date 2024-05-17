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
package com.falsepattern.falsetweaks.modules.debug;

import javax.swing.*;

public class Debug {
    public static final boolean ENABLED = Boolean.parseBoolean(System.getProperty("falsetweaks.debug", "false"));

    public static boolean occlusionChecks = true;
    public static boolean occlusionMask = true;
    public static boolean frustumChecks = true;
    public static boolean shadowPass = true;
    public static boolean neodymiumGC = true;
    public static boolean chunkRebaking = true;
    public static boolean shadowOcclusionChecks = true;
    public static boolean shadowOcclusionMask = true;
    public static boolean translucencySorting = true;
    public static boolean tesrRendering = true;

    public static void init() {
        SwingUtilities.invokeLater(Toggler::new);
    }
}
