/*
 * This file is part of FalseTweaks.
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

package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates;

import com.falsepattern.falsetweaks.modules.threadedupdates.ICapturableTessellator;
import com.falsepattern.falsetweaks.modules.threadedupdates.IRendererUpdateResultHolder;
import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadedChunkUpdateHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.EntityLivingBase;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin implements IRendererUpdateResultHolder {

    @Shadow
    protected abstract void postRenderBlocks(int p_147891_1_, EntityLivingBase p_147891_2_);

    private ThreadedChunkUpdateHelper.UpdateTask arch$updateTask;

    @Inject(method = "updateRenderer",
            at = @At("HEAD"),
            require = 1)
    private void setLastWorldRendererSingleton(CallbackInfo ci) {
        ThreadedChunkUpdateHelper.lastWorldRenderer = ((WorldRenderer) (Object) this);
    }

    @Redirect(method = "updateRenderer",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/WorldRenderer;postRenderBlocks(ILnet/minecraft/entity/EntityLivingBase;)V"),
              require = 1)
    private void loadTessellationResult(WorldRenderer instance, int pass, EntityLivingBase entity) {
        if (!ft$getRendererUpdateTask().cancelled) {
            ((ICapturableTessellator) Tessellator.instance).arch$addTessellatorVertexState(
                    ft$getRendererUpdateTask().result[pass].renderedQuads);
        }
        postRenderBlocks(pass, entity);
    }

    @Override
    public ThreadedChunkUpdateHelper.UpdateTask ft$getRendererUpdateTask() {
        if (arch$updateTask == null) {
            arch$updateTask = new ThreadedChunkUpdateHelper.UpdateTask();
        }
        return arch$updateTask;
    }

    @Inject(method = "markDirty",
            at = @At("RETURN"),
            require = 1)
    private void notifyDirty(CallbackInfo ci) {
        ThreadedChunkUpdateHelper.instance.onWorldRendererDirty((WorldRenderer) (Object) this);
    }

}
