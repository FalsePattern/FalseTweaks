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

import com.falsepattern.falsetweaks.api.ThreadedChunkUpdates;
import com.falsepattern.falsetweaks.modules.threadedupdates.IRendererUpdateResultHolder;
import com.falsepattern.falsetweaks.modules.threadedupdates.OptiFineCompat;
import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadedChunkUpdateHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.client.ForgeHooksClient;

@Mixin(RenderBlocks.class)
public abstract class RenderBlocksMixin {

    @Inject(method = "renderBlockByRenderType",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/block/Block;setBlockBoundsBasedOnState(Lnet/minecraft/world/IBlockAccess;III)V"),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void cancelRenderDelegatedToDifferentThread(Block block, int x, int y, int z, CallbackInfoReturnable<Boolean> cir, int renderType) {
        int pass = ForgeHooksClient.getWorldRenderPass();
        boolean mainThread = Thread.currentThread() == ThreadedChunkUpdateHelper.MAIN_THREAD;

        ThreadedChunkUpdateHelper.UpdateTask task = mainThread
                                                    ? ((IRendererUpdateResultHolder) ThreadedChunkUpdateHelper.lastWorldRenderer).ft$getRendererUpdateTask()
                                                    : null;

        boolean offThreadBlock = ThreadedChunkUpdateHelper.canBlockBeRenderedOffThread(block, pass, renderType) &&
                                 !(task != null && task.cancelled) &&
                                 (!mainThread || ThreadedChunkUpdateHelper.renderBlocksStack.getLevel() == 1);
        if ((mainThread ? pass >= 0 : true) && (mainThread ? offThreadBlock : !offThreadBlock)) {
            // Cancel rendering block if it's delegated to a different thread.
            cir.setReturnValue(mainThread ? task.result[pass].renderedSomething : false);
            OptiFineCompat.popEntity();
        }
    }

    @Inject(method = "renderBlockByRenderType",
            at = @At("HEAD"))
    private void pushStack(CallbackInfoReturnable<Boolean> cir) {
        ThreadedChunkUpdateHelper.renderBlocksStack.push();
    }

    @Inject(method = "renderBlockByRenderType",
            at = @At("RETURN"))
    private void popStack(CallbackInfoReturnable<Boolean> cir) {
        ThreadedChunkUpdateHelper.renderBlocksStack.pop();
    }

    @Redirect(method = "*",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/client/renderer/Tessellator;instance:Lnet/minecraft/client/renderer/Tessellator;"))
    private Tessellator modifyTessellatorAccess() {
        return ThreadedChunkUpdates.getThreadTessellator();
    }

}
