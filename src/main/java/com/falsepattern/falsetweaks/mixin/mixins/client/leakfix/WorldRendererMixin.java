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

package com.falsepattern.falsetweaks.mixin.mixins.client.leakfix;

import com.falsepattern.falsetweaks.modules.leakfix.LeakFix;
import com.falsepattern.falsetweaks.modules.leakfix.interfaces.IWorldRendererMixin;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.util.AxisAlignedBB;

@Mixin(WorldRenderer.class)
@Accessors(fluent = true)
public abstract class WorldRendererMixin implements IWorldRendererMixin {
    @Shadow
    public int posXClip;
    @Shadow
    public int posYClip;
    @Shadow
    public int posZClip;
    @Shadow
    private int glRenderList;
    @Getter
    private boolean hasRenderList;

    @Redirect(method = "<init>",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/WorldRenderer;setPosition(III)V"),
              require = 1)
    private void resetRenderListBefore(WorldRenderer thiz, int x, int y, int z) {
        if (LeakFix.ENABLED) {
            glRenderList = -1;
            hasRenderList = false;
        }
        thiz.setPosition(x, y, z);
    }

    @Inject(method = "setDontDraw",
            at = @At(value = "HEAD"),
            require = 1)
    private void clearLists(CallbackInfo ci) {
        if (LeakFix.ENABLED) {
            clearList();
        }
    }

    @Override
    public void renderAABB() {
        float extra = 6;
        GL11.glNewList(this.glRenderList + 2, 4864);
        RenderItem.renderAABB(
                AxisAlignedBB.getBoundingBox(this.posXClip - extra, this.posYClip - extra, this.posZClip - extra,
                                             this.posXClip + 16 + extra, this.posYClip + 16 + extra,
                                             this.posZClip + 16 + extra));
        GL11.glEndList();
    }

    @Override
    public boolean genList() {
        if (hasRenderList) {
            return false;
        }
        glRenderList = LeakFix.allocateWorldRendererBuffer();
        hasRenderList = true;
        return true;
    }

    @Override
    public boolean clearList() {
        if (!hasRenderList) {
            return false;
        }
        hasRenderList = false;
        LeakFix.releaseWorldRendererBuffer(glRenderList);
        glRenderList = -1;
        return true;
    }
}
