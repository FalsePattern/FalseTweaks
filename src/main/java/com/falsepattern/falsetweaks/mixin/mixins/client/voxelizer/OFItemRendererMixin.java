package com.falsepattern.falsetweaks.mixin.mixins.client.voxelizer;

import com.falsepattern.falsetweaks.modules.voxelizer.Data;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

@Pseudo
@Mixin(targets = "ItemRendererOF", remap = false)
@SuppressWarnings("UnresolvedMixinReference")
public abstract class OFItemRendererMixin {
    @Inject(method = "func_78443_a(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;I)V",
            at = @At("HEAD"),
            require = 1)
    private void startManagedMode(EntityLivingBase p_78443_1_, ItemStack p_78443_2_, int p_78443_3_, CallbackInfo ci) {
        Data.setManagedMode(true);
    }

    @Inject(method = "func_78443_a(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;I)V",
            at = @At("RETURN"),
            require = 1)
    private void endManagedMode(EntityLivingBase p_78443_1_, ItemStack p_78443_2_, int p_78443_3_, CallbackInfo ci) {
        Data.setManagedMode(false);
    }
}
