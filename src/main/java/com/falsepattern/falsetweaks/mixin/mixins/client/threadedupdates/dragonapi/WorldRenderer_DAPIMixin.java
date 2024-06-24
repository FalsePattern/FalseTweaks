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

package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates.dragonapi;

import com.falsepattern.falsetweaks.modules.threadedupdates.ThreadedChunkUpdateHelper;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.WorldRenderer;

@Mixin(WorldRenderer.class)
public abstract class WorldRenderer_DAPIMixin {

    @Dynamic
    @Inject(method = "updateRenderer",
            at = @At(value = "INVOKE",
                     target = "LReika/DragonAPI/Instantiable/Event/Client/RenderBlockAtPosEvent;fire(Lnet/minecraft/client/renderer/RenderBlocks;Lnet/minecraft/block/Block;IIILnet/minecraft/client/renderer/WorldRenderer;I)Z",
                     remap = false),
            require = 1)
    private void resetStackDragonAPI(CallbackInfo ci) {
        // Make sure the stack doesn't leak
        ThreadedChunkUpdateHelper.renderBlocksStack.reset();
    }

    @Inject(method = "updateRenderer",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/RenderBlocks;renderBlockByRenderType(Lnet/minecraft/block/Block;III)Z"),
            require = 1)
    private void resetStack(CallbackInfo ci) {
        // Make sure the stack doesn't leak
        ThreadedChunkUpdateHelper.renderBlocksStack.reset();
    }
}
