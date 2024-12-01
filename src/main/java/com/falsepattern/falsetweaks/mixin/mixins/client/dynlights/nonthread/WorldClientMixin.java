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

package com.falsepattern.falsetweaks.mixin.mixins.client.dynlights.nonthread;

import com.falsepattern.falsetweaks.modules.dynlights.DynamicLightsWorldClient;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.multiplayer.WorldClient;

@Mixin(WorldClient.class)
public abstract class WorldClientMixin implements DynamicLightsWorldClient {
    @Dynamic
    private boolean ft$renderItemInFirstPerson;

    @Override
    public boolean ft$renderItemInFirstPerson() {
        return ft$renderItemInFirstPerson;
    }
}
