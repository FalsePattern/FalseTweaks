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

package com.falsepattern.falsetweaks.modules.dynlights.base;

import lombok.val;
import mods.battlegear2.api.core.IInventoryPlayerBattle;
import mods.battlegear2.api.core.InventoryPlayerBattle;
import xonin.backhand.api.core.BackhandUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import cpw.mods.fml.common.Loader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum OffhandMod {
    Backhand {
        @Override
        public ItemStack getOffhandItem(EntityPlayer player) {
            return BackhandUtils.getOffhandItem(player);
        }
    },
    Battlegear2 {
        @Override
        public ItemStack getOffhandItem(EntityPlayer player) {
            return ((InventoryPlayerBattle) player.inventory).getCurrentOffhandWeapon();
        }
    },
    Battlegear2GTNH {
        @Override
        public ItemStack getOffhandItem(EntityPlayer player) {
            return ((IInventoryPlayerBattle) player.inventory).battlegear2$getCurrentOffhandWeapon();
        }
    };

    public static final List<OffhandMod> CURRENT = detectCurrent();

    private static List<OffhandMod> detectCurrent() {
        val result = new ArrayList<OffhandMod>();
        if (Loader.isModLoaded("battlegear2")) {
            try {
                if (Launch.classLoader.getClassBytes("mods.battlegear2.api.core.InventoryPlayerBattle") != null) {
                    result.add(Battlegear2);
                }
            } catch (Throwable ignored) {
            }
            try {
                if (Launch.classLoader.getClassBytes("mods.battlegear2.api.core.IInventoryPlayerBattle") != null) {
                    result.add(Battlegear2GTNH);
                }
            } catch (Throwable ignored) {}
        }
        if (Loader.isModLoaded("backhand")) {
            result.add(Backhand);
        }
        return Collections.unmodifiableList(result);
    }

    public abstract ItemStack getOffhandItem(EntityPlayer player);
}
