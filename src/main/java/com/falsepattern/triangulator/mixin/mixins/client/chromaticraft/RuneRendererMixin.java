package com.falsepattern.triangulator.mixin.mixins.client.chromaticraft;

import Reika.ChromatiCraft.Render.ISBRH.RuneRenderer;
import com.falsepattern.triangulator.mixin.helper.IRenderBlocksMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

@Mixin(value = RuneRenderer.class,
       remap = false)
public abstract class RuneRendererMixin {
    @Inject(method = "renderInventoryBlock",
            at = @At(value = "INVOKE",
                     target = "LReika/ChromatiCraft/Registry/CrystalElement;getFaceRune()Lnet/minecraft/util/IIcon;"),
            require = 1)
    private void enableReuse(Block b, int metadata, int modelId, RenderBlocks rb, CallbackInfo ci) {
        ((IRenderBlocksMixin) rb).reusePreviousStates(true);
    }

    @Inject(method = "renderWorldBlock",
            at = @At(value = "INVOKE",
                     target = "LReika/ChromatiCraft/Registry/CrystalElement;getFaceRune()Lnet/minecraft/util/IIcon;"),
            require = 1)
    private void enableReuse(IBlockAccess world, int x, int y, int z, Block b, int modelId, RenderBlocks rb, CallbackInfoReturnable<Boolean> cir) {
        ((IRenderBlocksMixin) rb).reusePreviousStates(true);
    }

    @Inject(method = "renderInventoryBlock",
            at = @At(value = "RETURN"),
            require = 1)
    private void disableReuse(Block b, int metadata, int modelId, RenderBlocks rb, CallbackInfo ci) {
        ((IRenderBlocksMixin) rb).reusePreviousStates(false);
    }

    @Inject(method = "renderWorldBlock",
            at = @At(value = "RETURN"),
            require = 1)
    private void disableReuse(IBlockAccess world, int x, int y, int z, Block b, int modelId, RenderBlocks rb, CallbackInfoReturnable<Boolean> cir) {
        ((IRenderBlocksMixin) rb).reusePreviousStates(false);
    }
}
