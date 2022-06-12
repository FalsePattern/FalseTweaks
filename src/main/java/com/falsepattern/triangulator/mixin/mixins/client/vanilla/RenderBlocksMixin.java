package com.falsepattern.triangulator.mixin.mixins.client.vanilla;

import com.falsepattern.triangulator.TriConfig;
import com.falsepattern.triangulator.mixin.helper.ITessellatorMixin;
import lombok.val;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderBlocks.class)
public abstract class RenderBlocksMixin {

    @Shadow(aliases = "colorRedTopLeftF")
    public float colorRedTopLeft;

    @Shadow(aliases = "colorGreenTopLeftF")
    public float colorGreenTopLeft;

    @Shadow(aliases = "colorBlueTopLeftF")
    public float colorBlueTopLeft;

    @Shadow(aliases = "colorRedBottomLeftF")
    public float colorRedBottomLeft;

    @Shadow(aliases = "colorGreenBottomLeftF")
    public float colorGreenBottomLeft;

    @Shadow(aliases = "colorBlueBottomLeftF")
    public float colorBlueBottomLeft;

    @Shadow(aliases = "colorRedBottomRightF")
    public float colorRedBottomRight;

    @Shadow(aliases = "colorGreenBottomRightF")
    public float colorGreenBottomRight;

    @Shadow(aliases = "colorBlueBottomRightF")
    public float colorBlueBottomRight;

    @Shadow(aliases = "colorRedTopRightF")
    public float colorRedTopRight;

    @Shadow(aliases = "colorGreenTopRightF")
    public float colorGreenTopRight;

    @Shadow(aliases = "colorBlueTopRightF")
    public float colorBlueTopRight;

    private static float avg(final float a, final float b) {
        return (a + b) * 0.5F;
    }

    private static float avg(final float r, final float g, final float b) {
        return (r + g + b) * 0.3333333333333333F;
    }

    private static float diff(final float a, final float b) {
        return Math.abs(a - b);
    }

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
        if (!TriConfig.ENABLE_QUAD_TRIANGULATION) return;
        val avgTopLeft = avg(colorRedTopLeft, colorGreenTopLeft, colorBlueTopLeft);
        val avgBottomLeft = avg(colorRedBottomLeft, colorGreenBottomLeft, colorBlueBottomLeft);
        val avgBottomRight = avg(colorRedBottomRight, colorGreenBottomRight, colorBlueBottomRight);
        val avgTopRight = avg(colorRedTopRight, colorGreenTopRight, colorBlueTopRight);
        val mainDiagonalDiff = diff(avgTopLeft, avgBottomRight);
        val altDiagonalDiff = diff(avgBottomLeft, avgTopRight);
        if (Math.abs(mainDiagonalDiff - altDiagonalDiff) < 0.01) {
            val mainDiagonalAvg = avg(avgTopLeft, avgBottomRight);
            val altDiagonalAvg = avg(avgBottomLeft, avgTopRight);
            if (mainDiagonalAvg < altDiagonalAvg) {
                ((ITessellatorMixin) Tessellator.instance).setAlternativeTriangulation();
            }
        } else if (altDiagonalDiff < mainDiagonalDiff) {
            ((ITessellatorMixin) Tessellator.instance).setAlternativeTriangulation();
        }
    }

    /**
     * @author embeddedt
     */
    @ModifyArg(method = {"renderStandardBlockWithAmbientOcclusion", "renderStandardBlockWithAmbientOcclusionPartial"},
               slice = @Slice(
                       from = @At(value = "FIELD",
                                  target = "Lnet/minecraft/client/renderer/RenderBlocks;aoLightValueScratchXYPN:F",
                                  opcode = Opcodes.PUTFIELD,
                                  ordinal = 0),
                       to = @At(value = "FIELD",
                                target = "Lnet/minecraft/client/renderer/RenderBlocks;aoLightValueScratchXYZNNN:F",
                                opcode = Opcodes.PUTFIELD,
                                ordinal = 0)
               ),
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/world/IBlockAccess;getBlock(III)Lnet/minecraft/block/Block;"),
               index = 1,
               allow = 8)
    private int incrementYValue0(int y) {
        return y + 1;
    }

    int countS;
    int countB;
    float sky;
    float block;

    private void addLight(int light) {
        int S = light & 0xff;
        int B = (light & 0xff0000) >>> 16;
        if (S != 0) {
            sky += S;
            countS++;
        }
        if (B != 0) {
            block += B;
            countB++;
        }
    }

    @Inject(method = "getAoBrightness",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private void betterCompute(int a, int b, int c, int d, CallbackInfoReturnable<Integer> cir) {
        countS = 0;
        countB = 0;
        sky = 0;
        block = 0;
        addLight(a);
        addLight(b);
        addLight(c);
        addLight(d);
        sky /= countS;
        block /= countB;
        cir.setReturnValue((((int)sky) & 0xff) | ((((int)block) & 0xff) << 16));
    }

    /**
     * @author embeddedt
     */
    @ModifyArg(method = {"renderStandardBlockWithAmbientOcclusion", "renderStandardBlockWithAmbientOcclusionPartial"},
               slice = @Slice(
                       from = @At(value = "FIELD",
                                  target = "Lnet/minecraft/client/renderer/RenderBlocks;aoLightValueScratchYZPP:F",
                                  opcode = Opcodes.PUTFIELD,
                                  ordinal = 0),
                       to = @At(value = "FIELD",
                                target = "Lnet/minecraft/client/renderer/RenderBlocks;aoLightValueScratchXYZNPN:F",
                                opcode = Opcodes.PUTFIELD,
                                ordinal = 0)
               ),
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/world/IBlockAccess;getBlock(III)Lnet/minecraft/block/Block;"),
               index = 1,
               allow = 8)
    private int decrementYValue1(int y) {
        return y - 1;
    }

    /**
     * @author embeddedt
     */
    @ModifyArg(method = {"renderStandardBlockWithAmbientOcclusion", "renderStandardBlockWithAmbientOcclusionPartial"},
               slice = @Slice(
                       from = @At(value = "FIELD",
                                  target = "Lnet/minecraft/client/renderer/RenderBlocks;aoBrightnessXZPN:I",
                                  opcode = Opcodes.PUTFIELD,
                                  ordinal = 0),
                       to = @At(value = "FIELD",
                                target = "Lnet/minecraft/client/renderer/RenderBlocks;aoLightValueScratchXYZNNN:F",
                                opcode = Opcodes.PUTFIELD,
                                ordinal = 2)
               ),
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/world/IBlockAccess;getBlock(III)Lnet/minecraft/block/Block;"),
               index = 2,
               allow = 8)
    private int incrementZValue2(int z) {
        return z + 1;
    }

    /**
     * @author embeddedt
     */
    @ModifyArg(method = {"renderStandardBlockWithAmbientOcclusion", "renderStandardBlockWithAmbientOcclusionPartial"},
               slice = @Slice(
                       from = @At(value = "FIELD",
                                  target = "Lnet/minecraft/client/renderer/RenderBlocks;aoLightValueScratchYZPP:F",
                                  opcode = Opcodes.PUTFIELD,
                                  ordinal = 1),
                       to = @At(value = "FIELD",
                                target = "Lnet/minecraft/client/renderer/RenderBlocks;aoLightValueScratchXYZNNP:F",
                                opcode = Opcodes.PUTFIELD,
                                ordinal = 2)
               ),
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/world/IBlockAccess;getBlock(III)Lnet/minecraft/block/Block;"),
               index = 2,
               allow = 8)
    private int decrementZValue3(int z) {
        return z - 1;
    }

    /**
     * @author embeddedt
     */
    @ModifyArg(method = {"renderStandardBlockWithAmbientOcclusion", "renderStandardBlockWithAmbientOcclusionPartial"},
               slice = @Slice(
                       from = @At(value = "FIELD",
                                  target = "Lnet/minecraft/client/renderer/RenderBlocks;aoLightValueScratchXYNP:F",
                                  opcode = Opcodes.PUTFIELD,
                                  ordinal = 1),
                       to = @At(value = "FIELD",
                                target = "Lnet/minecraft/client/renderer/RenderBlocks;aoLightValueScratchXYZNNN:F",
                                opcode = Opcodes.PUTFIELD,
                                ordinal = 4)
               ),
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/world/IBlockAccess;getBlock(III)Lnet/minecraft/block/Block;"),
               index = 0,
               allow = 8)
    private int incrementXValue4(int x) {
        return x + 1;
    }

    /**
     * @author embeddedt
     */
    @ModifyArg(method = {"renderStandardBlockWithAmbientOcclusion", "renderStandardBlockWithAmbientOcclusionPartial"},
               slice = @Slice(
                       from = @At(value = "FIELD",
                                  target = "Lnet/minecraft/client/renderer/RenderBlocks;aoLightValueScratchXYPP:F",
                                  opcode = Opcodes.PUTFIELD,
                                  ordinal = 1),
                       to = @At(value = "FIELD",
                                target = "Lnet/minecraft/client/renderer/RenderBlocks;aoLightValueScratchXYZPNN:F",
                                opcode = Opcodes.PUTFIELD,
                                ordinal = 4)
               ),
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/world/IBlockAccess;getBlock(III)Lnet/minecraft/block/Block;"),
               index = 0,
               allow = 8)
    private int decrementXValue5(int x) {
        return x - 1;
    }
}
