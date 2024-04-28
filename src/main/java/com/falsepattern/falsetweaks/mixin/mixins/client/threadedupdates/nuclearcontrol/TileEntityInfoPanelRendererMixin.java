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
package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates.nuclearcontrol;

import com.falsepattern.falsetweaks.Compat;
import lombok.val;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shedar.mods.ic2.nuclearcontrol.renderers.TileEntityInfoPanelRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

@Mixin(TileEntityInfoPanelRenderer.class)
public abstract class TileEntityInfoPanelRendererMixin extends TileEntitySpecialRenderer {
    @Inject(method = "renderTileEntityAt",
            at = @At("HEAD"),
            require = 1)
    private void renderActualBlock(TileEntity tileEntity, double x, double y, double z, float f, CallbackInfo ci) {
        val mc = Minecraft.getMinecraft();
        bindTexture(TextureMap.locationBlocksTexture);
        val renderBlocks = RenderBlocks.getInstance();
        renderBlocks.blockAccess = mc.theWorld;

        val tess = Compat.tessellator();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_LIGHTING);

        tess.startDrawing(GL11.GL_QUADS);
        tess.setTranslation(x - tileEntity.xCoord, y - tileEntity.yCoord, z - tileEntity.zCoord);

        renderBlocks.renderBlockByRenderType(tileEntity.blockType, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);

        tess.setTranslation(0D, 0D, 0D);
        tess.draw();

        GL11.glPopAttrib();
    }
}
