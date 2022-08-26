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

package com.falsepattern.animfix.mixin.plugin;

import com.falsepattern.lib.mixin.IMixin;
import com.falsepattern.lib.mixin.ITargetedMod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Predicate;

import static com.falsepattern.lib.mixin.IMixin.PredicateHelpers.always;
import static com.falsepattern.lib.mixin.IMixin.PredicateHelpers.require;

@RequiredArgsConstructor
public enum Mixin implements IMixin {
    //region Minecraft->client
    TextureMapMixin(Side.CLIENT, always(), "minecraft.TextureMapMixin"),
    TextureUtilMixin(Side.CLIENT, always(), "minecraft.TextureUtilMixin"),
    StitcherMixin(Side.CLIENT, always(), "minecraft.StitcherMixin"),
    StitcherSlotMixin(Side.CLIENT, always(), "minecraft.StitcherSlotMixin"), //endregion Minecraft->client
    //region FastCraft->client
    FCAbstractTextureMixin(Side.CLIENT, require(TargetedMod.FASTCRAFT), "fastcraft.AbstractTextureMixin"),
    FCDynamicTextureMixin(Side.CLIENT, require(TargetedMod.FASTCRAFT), "fastcraft.DynamicTextureMixin"),
    FCTextureMapMixin(Side.CLIENT, require(TargetedMod.FASTCRAFT), "fastcraft.TextureMapMixin"),
    FCTextureUtilMixin(Side.CLIENT, require(TargetedMod.FASTCRAFT), "fastcraft.TextureUtilMixin"),
    //endregion FastCraft->client
    ;

    @Getter
    private final Side side;
    @Getter
    private final Predicate<List<ITargetedMod>> filter;
    @Getter
    private final String mixin;
}
