package com.falsepattern.falsetweaks.mixin.mixins.client.rendersafety;

import com.falsepattern.falsetweaks.config.RenderingSafetyConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lombok.val;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

@Mixin(value = RenderingRegistry.class,
       remap = false)
public abstract class RenderingRegistryMixin {
    @WrapOperation(method = "renderInventoryBlock",
                   at = @At(value = "INVOKE",
                       target = "Lcpw/mods/fml/client/registry/ISimpleBlockRenderingHandler;renderInventoryBlock(Lnet/minecraft/block/Block;IILnet/minecraft/client/renderer/RenderBlocks;)V"),
                   require = 1)
    private void wrapBlock(ISimpleBlockRenderingHandler instance, Block block, int metadata, int modelID, RenderBlocks renderer, Operation<Void> original) {
        val enable = RenderingSafetyConfig.ENABLE_BLOCK;
        if (enable)
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        original.call(instance, block, metadata, modelID, renderer);
        if (enable)
            GL11.glPopAttrib();
    }
}
