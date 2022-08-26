/*
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

package com.falsepattern.animfix.config;

import com.falsepattern.animfix.Tags;
import com.falsepattern.lib.config.ConfigException;
import com.falsepattern.lib.config.SimpleGuiConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class AnimFixGuiConfig extends SimpleGuiConfig {

    public AnimFixGuiConfig(GuiScreen parent) throws ConfigException {
        super(parent, AnimConfig.class, Tags.MODID, Tags.MODNAME);
    }

    @Override
    public void onGuiClosed() {
        Minecraft.getMinecraft().scheduleResourcesRefresh();
    }
}
