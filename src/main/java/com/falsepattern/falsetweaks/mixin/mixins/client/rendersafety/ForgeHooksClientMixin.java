package com.falsepattern.falsetweaks.mixin.mixins.client.rendersafety;

import com.falsepattern.falsetweaks.config.RenderingSafetyConfig;
import com.falsepattern.falsetweaks.modules.rendersafety.SafetyUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.IItemRenderer;

@Mixin(value = ForgeHooksClient.class,
       remap = false)
public abstract class ForgeHooksClientMixin {
    @WrapOperation(method = {"renderEntityItem", "renderInventoryItem", "renderEquippedItem"},
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraftforge/client/IItemRenderer;renderItem(Lnet/minecraftforge/client/IItemRenderer$ItemRenderType;Lnet/minecraft/item/ItemStack;[Ljava/lang/Object;)V"),
                   require = 0, // Weak mixin to avoid crashes
                   expect = 6)
    private static void wrapRenderItem(IItemRenderer instance, IItemRenderer.ItemRenderType itemRenderType, ItemStack itemStack, Object[] objects, Operation<Void> original) {
        val enable = RenderingSafetyConfig.ENABLE_ITEM;
        SafetyUtil.pre(enable);
        original.call(instance, itemRenderType, itemStack, objects);
        SafetyUtil.post(enable);
    }
}
