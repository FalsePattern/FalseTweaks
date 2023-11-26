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
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.client.ForgeHooksClient;

@Mixin(RenderBlocks.class)
public abstract class RenderBlocksMixin {

    private int returnBoolCancel;
    /**
     * @author FalsePattern
     * @reason CallbackInfoReturnable from a regular injection is really expensive and allocation spammy,
     * so we sidestep it by shuffling data through a field
     */
    @Redirect(method = "renderBlockByRenderType",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/block/Block;getRenderType()I",
                       ordinal = 0),
              require = 1)
    private int cancelRenderDelegatedToDifferentThread1(Block block) {
        returnBoolCancel = 0;
        int renderType = block.getRenderType();
        int pass = ForgeHooksClient.getWorldRenderPass();
        boolean mainThread = Thread.currentThread() == ThreadedChunkUpdateHelper.MAIN_THREAD;

        boolean renderableOffThread = ThreadedChunkUpdateHelper.canBlockBeRenderedOffThread(block, pass, renderType);
        if (mainThread) {
            val task = ((IRendererUpdateResultHolder) ThreadedChunkUpdateHelper.lastWorldRenderer).ft$getRendererUpdateTask();

            if (task != null && !task.cancelled && renderableOffThread && pass >= 0) {
                returnBoolCancel = task.result[pass].renderedSomething ? 1 : 0;
                return -1;
            }
        } else if (!renderableOffThread) {
            return -1;
        }
        return renderType;
    }

    //Minecraft Dev plugin for intellij incorrectly assumes this to the "0" in the switch
    //In reality, this replaces the "return false;" statement in the -1 case
    @ModifyConstant(method = "renderBlockByRenderType",
                    constant = @Constant(intValue = 0,
                                         ordinal = 0),
                    require = 1)
    private int fakeReturn(int value) {
        return returnBoolCancel;
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
