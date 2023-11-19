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

    @Shadow protected abstract void postRenderBlocks(int p_147891_1_, EntityLivingBase p_147891_2_);

    private ThreadedChunkUpdateHelper.UpdateTask arch$updateTask;

    @Inject(method = "updateRenderer",
            at = @At("HEAD"),
            require = 1)
    private void setLastWorldRendererSingleton(CallbackInfo ci) {
        ThreadedChunkUpdateHelper.lastWorldRenderer = ((WorldRenderer)(Object)this);
    }

    @Redirect(method = "updateRenderer",
              at = @At(value="INVOKE", target = "Lnet/minecraft/client/renderer/WorldRenderer;postRenderBlocks(ILnet/minecraft/entity/EntityLivingBase;)V"),
              require = 1)
    private void loadTessellationResult(WorldRenderer instance, int pass, EntityLivingBase entity) {
        if(!ft$getRendererUpdateTask().cancelled) {
            ((ICapturableTessellator) Tessellator.instance).arch$addTessellatorVertexState(
                    ft$getRendererUpdateTask().result[pass].renderedQuads);
        }
        postRenderBlocks(pass, entity);
    }

    @Inject(method = "updateRenderer",
            at = @At(value="INVOKE",
                     target = "Lnet/minecraft/client/renderer/RenderBlocks;renderBlockByRenderType(Lnet/minecraft/block/Block;III)Z"),
            require = 1)
    private void resetStack(CallbackInfo ci) {
        // Make sure the stack doesn't leak
        ThreadedChunkUpdateHelper.renderBlocksStack.reset();
    }

    @Override
    public ThreadedChunkUpdateHelper.UpdateTask ft$getRendererUpdateTask() {
        if(arch$updateTask == null) {
            arch$updateTask = new ThreadedChunkUpdateHelper.UpdateTask();
        }
        return arch$updateTask;
    }

    @Inject(method = "markDirty",
            at = @At("RETURN"),
            require = 1)
    private void notifyDirty(CallbackInfo ci) {
        ThreadedChunkUpdateHelper.instance.onWorldRendererDirty((WorldRenderer)(Object)this);
    }

}
