/*
 * FalseTweaks
 *
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice, this permission notice and the word "SNEED"
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.falsetweaks.mixin.plugin.preinit;

import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.lib.mixin.IMixin;
import com.falsepattern.lib.mixin.IMixinPlugin;
import com.falsepattern.lib.mixin.ITargetedMod;
import lombok.Getter;
import org.apache.logging.log4j.Logger;

public class MixinPlugin implements IMixinPlugin {
    @Getter
    private final Logger logger = IMixinPlugin.createLogger(Tags.MODNAME + " PreInit");

    @Override
    public ITargetedMod[] getTargetedModEnumValues() {
        return new ITargetedMod[0];
    }

    @Override
    public IMixin[] getMixinEnumValues() {
        return Mixin.values();
    }
}
