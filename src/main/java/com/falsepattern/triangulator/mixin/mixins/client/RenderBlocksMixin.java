package com.falsepattern.triangulator.mixin.mixins.client;

import com.falsepattern.triangulator.mixin.helper.ITessellatorMixin;
import lombok.val;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderBlocks.class)
public abstract class RenderBlocksMixin {

    @Shadow public float colorRedTopLeft;

    @Shadow public float colorGreenTopLeft;

    @Shadow public float colorBlueTopLeft;

    @Shadow public float colorRedBottomLeft;

    @Shadow public float colorGreenBottomLeft;

    @Shadow public float colorBlueBottomLeft;

    @Shadow public float colorRedBottomRight;

    @Shadow public float colorGreenBottomRight;

    @Shadow public float colorBlueBottomRight;

    @Shadow public float colorRedTopRight;

    @Shadow public float colorGreenTopRight;

    @Shadow public float colorBlueTopRight;

    @Inject(method = {"renderStandardBlockWithAmbientOcclusion", "renderStandardBlockWithAmbientOcclusionPartial"},
            at = {
                    @At(value = "INVOKE",
                        target = "Lnet/minecraft/client/renderer/RenderBlocks;renderFaceXNeg(Lnet/minecraft/block/Block;DDDLnet/minecraft/util/IIcon;)V"),
                    @At(value = "INVOKE",
                        target = "Lnet/minecraft/client/renderer/RenderBlocks;renderFaceXPos(Lnet/minecraft/block/Block;DDDLnet/minecraft/util/IIcon;)V"),
                    @At(value = "INVOKE",
                        target = "Lnet/minecraft/client/renderer/RenderBlocks;renderFaceYNeg(Lnet/minecraft/block/Block;DDDLnet/minecraft/util/IIcon;)V"),
                    @At(value = "INVOKE",
                        target = "Lnet/minecraft/client/renderer/RenderBlocks;renderFaceYPos(Lnet/minecraft/block/Block;DDDLnet/minecraft/util/IIcon;)V"),
                    @At(value = "INVOKE",
                        target = "Lnet/minecraft/client/renderer/RenderBlocks;renderFaceZNeg(Lnet/minecraft/block/Block;DDDLnet/minecraft/util/IIcon;)V"),
                    @At(value = "INVOKE",
                        target = "Lnet/minecraft/client/renderer/RenderBlocks;renderFaceZPos(Lnet/minecraft/block/Block;DDDLnet/minecraft/util/IIcon;)V"),
            },
            require = 12)
    private void aoFix(CallbackInfoReturnable<Boolean> cir) {
        val avgTopLeft = (colorRedTopLeft + colorGreenTopLeft + colorBlueTopLeft) / 3.0f;
        val avgBottomLeft = (colorRedBottomLeft + colorGreenBottomLeft + colorBlueBottomLeft) / 3.0f;
        val avgBottomRight = (colorRedBottomRight + colorGreenBottomRight + colorBlueBottomRight) / 3.0f;
        val avgTopRight = (colorRedTopRight + colorGreenTopRight + colorBlueTopRight) / 3.0f;
        val mainDiagonalDiff = Math.abs(avgTopLeft - avgBottomRight);
        val altDiagonalDiff = Math.abs(avgBottomLeft - avgTopRight);
        if (Math.abs(mainDiagonalDiff - altDiagonalDiff) < 0.01) {
            val mainDiagonalAvg = (avgTopLeft + avgBottomRight) * 0.5F;
            val altDiagonalAvg = (avgBottomLeft + avgTopRight) * 0.5F;
            if (mainDiagonalAvg < altDiagonalAvg) {
                ((ITessellatorMixin) Tessellator.instance).setAlternativeTriangulation();
            }
        } else if (altDiagonalDiff < mainDiagonalDiff) {
            ((ITessellatorMixin) Tessellator.instance).setAlternativeTriangulation();
        }
    }
}
