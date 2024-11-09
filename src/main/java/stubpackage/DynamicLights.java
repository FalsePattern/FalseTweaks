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
package stubpackage;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

public class DynamicLights {

    public static void entityAdded(Entity entityIn, RenderGlobal renderGlobal) {

    }

    public static void entityRemoved(Entity entityIn, RenderGlobal renderGlobal) {

    }

    public static void update(RenderGlobal renderGlobal) {

    }

    private static void updateMapDynamicLights(RenderGlobal renderGlobal) {

    }

    public static int getCombinedLight(int x, int y, int z, int combinedLight) {
        return 0;
    }

    public static int getCombinedLight(Entity entity, int combinedLight) {
        return 0;
    }

    public static int getCombinedLight(double lightPlayer, int combinedLight) {
        return 0;
    }

    public static double getLightLevel(int x, int y, int z) {
        return 0;
    }

    public static int getLightLevel(ItemStack itemStack) {
        return 0;
    }

    public static int getLightLevel(Entity entity) {
        return 0;
    }

    public static void removeLights(RenderGlobal renderGlobal) {

    }

    public static void clear() {

    }

    public static int getCount() {
        return 0;
    }

    public static ItemStack getItemStack(EntityItem entityItem) {
        return null;
    }
}
