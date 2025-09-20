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

package stubpackage.net.minecraft.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;

public class EntityRenderer extends net.minecraft.client.renderer.EntityRenderer {
    /**
     * OptiFine-added field
     */
    public boolean fogStandard;

    public EntityRenderer(Minecraft p_i45076_1_, IResourceManager p_i45076_2_) {
        super(p_i45076_1_, p_i45076_2_);
    }
}
