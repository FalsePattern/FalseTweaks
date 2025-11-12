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

import mods.battlegear2.api.core.IInventoryPlayerBattle;
import mods.battlegear2.api.core.InventoryPlayerBattle;
import xonin.backhand.api.core.BackhandUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import cpw.mods.fml.common.Loader;

public enum OffhandMod {
    None,
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

    public static final OffhandMod CURRENT = detectCurrent();

    private static OffhandMod detectCurrent() {
        if (Loader.isModLoaded("battlegear2")) {
            try {
                if (Launch.classLoader.getClassBytes("mods.battlegear2.api.core.InventoryPlayerBattle") != null) {
                    return OffhandMod.Battlegear2;
                }
            } catch (Throwable ignored) {
            }
            return OffhandMod.Battlegear2GTNH;
        }
        if (Loader.isModLoaded("backhand")) {
            return OffhandMod.Backhand;
        }
        return None;
    }

    public ItemStack getOffhandItem(EntityPlayer player) {
        return null;
    }
}
