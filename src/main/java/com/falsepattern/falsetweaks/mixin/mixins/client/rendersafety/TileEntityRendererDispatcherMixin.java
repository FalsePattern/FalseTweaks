package com.falsepattern.falsetweaks.mixin.mixins.client.rendersafety;

import com.falsepattern.falsetweaks.config.RenderingSafetyConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lombok.val;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

@Mixin(TileEntityRendererDispatcher.class)
public abstract class TileEntityRendererDispatcherMixin {
    @WrapOperation(method = "renderTileEntityAt",
                   at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/tileentity/TileEntitySpecialRenderer;renderTileEntityAt(Lnet/minecraft/tileentity/TileEntity;DDDF)V"),
                   require = 1)
    private void wrapTESR(TileEntitySpecialRenderer instance, TileEntity entity, double x, double y, double z, float tickDelta, Operation<Void> original) {
        val enable = RenderingSafetyConfig.ENABLE_TESR;
        if (enable)
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        original.call(instance, entity, x, y, z, tickDelta);
        if (enable)
            GL11.glPopAttrib();
    }
}
