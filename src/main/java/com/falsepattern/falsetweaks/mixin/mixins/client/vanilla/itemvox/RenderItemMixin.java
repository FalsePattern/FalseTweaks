/*
 * Triangulator
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

package com.falsepattern.falsetweaks.mixin.mixins.client.vanilla.itemvox;

import com.falsepattern.falsetweaks.TriCompat;
import com.falsepattern.falsetweaks.renderlists.VoxelRenderListManager;
import com.falsepattern.falsetweaks.config.FTConfig;
import com.falsepattern.falsetweaks.voxelizer.VoxelMesh;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.IIcon;

@Mixin(RenderItem.class)
public abstract class RenderItemMixin {
    @Inject(method = "renderDroppedItem(Lnet/minecraft/entity/item/EntityItem;Lnet/minecraft/util/IIcon;IFFFFI)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/ItemRenderer;renderItemIn2D(Lnet/minecraft/client/renderer/Tessellator;FFFFIIF)V",
                     ordinal = 0),
            require = 1)
    private void voxelizedRender(EntityItem p_77020_1_, IIcon iicon, int p_77020_3_, float p_77020_4_, float p_77020_5_, float p_77020_6_, float p_77020_7_, int pass, CallbackInfo ci) {
        val mesh = VoxelMesh.getMesh((TextureAtlasSprite) iicon);
        if (FTConfig.ENABLE_ITEM_RENDERLISTS && VoxelRenderListManager.INSTANCE.pre(mesh)) {
            return;
        }
        val tess = TriCompat.tessellator();
        tess.startDrawingQuads();
        mesh.renderToTessellator(tess);
        tess.draw();
        if (FTConfig.ENABLE_ITEM_RENDERLISTS) {
            VoxelRenderListManager.INSTANCE.post();
        }
    }

    @Redirect(method = "renderDroppedItem(Lnet/minecraft/entity/item/EntityItem;Lnet/minecraft/util/IIcon;IFFFFI)V",
              at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/ItemRenderer;renderItemIn2D(Lnet/minecraft/client/renderer/Tessellator;FFFFIIF)V",
                     ordinal = 0),
              require = 1)
    private void voxelizedRendererKillOriginal(Tessellator tess, float a, float b, float c, float d, int e, int f, float g) {

    }
}
