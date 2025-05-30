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

package com.falsepattern.falsetweaks.modules.threadedupdates;

import com.falsepattern.falsetweaks.asm.modules.threadedupdates.settings.Threading_GameSettingsRedirector;

/**
 * These methods are injected as redirects using ASM.
 *
 * @implNote I REALLY hate that people tamper with this mid-renders. -Ven
 * @see Threading_GameSettingsRedirector
 */
@SuppressWarnings("unused")
public interface ThreadSafeSettings {
    void ft$update();

    void ft$fancyGraphics(boolean fancyGraphics);

    boolean ft$fancyGraphics();
}
