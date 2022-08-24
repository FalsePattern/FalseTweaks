package com.falsepattern.triangulator.mixin.mixins.client.vanilla;

import com.falsepattern.triangulator.api.ToggleableTessellator;
import com.falsepattern.triangulator.calibration.CalibrationConfig;
import com.falsepattern.triangulator.mixin.helper.IRenderBlocksMixin;
import com.falsepattern.triangulator.mixin.helper.ITessellatorMixin;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import lombok.var;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

@Mixin(RenderBlocks.class)
@Accessors(fluent = true,
           chain = false)
public abstract class RenderBlocksMixin implements IRenderBlocksMixin {

    @Shadow
    public float colorRedTopLeft;

    @Shadow
    public float colorGreenTopLeft;

    @Shadow
    public float colorBlueTopLeft;

    @Shadow
    public float colorRedBottomLeft;

    @Shadow
    public float colorGreenBottomLeft;

    @Shadow
    public float colorBlueBottomLeft;

    @Shadow
    public float colorRedBottomRight;

    @Shadow
    public float colorGreenBottomRight;

    @Shadow
    public float colorBlueBottomRight;

    @Shadow
    public float colorRedTopRight;

    @Shadow
    public float colorGreenTopRight;

    @Shadow
    public float colorBlueTopRight;
    int countS;
    int countB;
    float sky;
    float block;
    private boolean[] states;
    @Setter
    private boolean reusePreviousStates;

    private static float avg(final float a, final float b) {
        return (a + b) * 0.5F;
    }

    private static float avg(final float r, final float g, final float b) {
        return (r + g + b) * 0.3333333333333333F;
    }

    private static float diff(final float a, final float b) {
        return Math.abs(a - b);
    }

    @Inject(method = {"<init>()V", "<init>(Lnet/minecraft/world/IBlockAccess;)V"},
            at = @At(value = "RETURN"),
            require = 2)
    private void setupStates(CallbackInfo ci) {
        states = new boolean[6];
    }

    @Inject(method = {"renderStandardBlockWithAmbientOcclusion", "renderStandardBlockWithAmbientOcclusionPartial"},
            at = {@At(value = "INVOKE",
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
                      target = "Lnet/minecraft/client/renderer/RenderBlocks;renderFaceZPos(Lnet/minecraft/block/Block;DDDLnet/minecraft/util/IIcon;)V"),},
            require = 12)
    private void aoFix(CallbackInfoReturnable<Boolean> cir) {
        if (reusePreviousStates) {
            return;
        }
        var avgTopLeft = avg(colorRedTopLeft, colorGreenTopLeft, colorBlueTopLeft);
        var avgBottomLeft = avg(colorRedBottomLeft, colorGreenBottomLeft, colorBlueBottomLeft);
        var avgBottomRight = avg(colorRedBottomRight, colorGreenBottomRight, colorBlueBottomRight);
        var avgTopRight = avg(colorRedTopRight, colorGreenTopRight, colorBlueTopRight);
        if (((ToggleableTessellator) Tessellator.instance).isTriangulatorDisabled() &&
            CalibrationConfig.FLIP_DIAGONALS) {
            var tmp = avgTopLeft;
            avgTopLeft = avgBottomLeft;
            avgBottomLeft = tmp;
            tmp = avgTopRight;
            avgTopRight = avgBottomRight;
            avgBottomRight = tmp;
        }
        val mainDiagonalDiff = diff(avgTopLeft, avgBottomRight);
        val altDiagonalDiff = diff(avgBottomLeft, avgTopRight);
        if (Math.abs(mainDiagonalDiff - altDiagonalDiff) < 0.01) {
            val mainDiagonalAvg = avg(avgTopLeft, avgBottomRight);
            val altDiagonalAvg = avg(avgBottomLeft, avgTopRight);
            if (Math.abs(mainDiagonalAvg - altDiagonalAvg) > 0.01 && mainDiagonalAvg < altDiagonalAvg) {
                ((ITessellatorMixin) Tessellator.instance).alternativeTriangulation(true);
                return;
            }
        } else if (altDiagonalDiff < mainDiagonalDiff) {
            ((ITessellatorMixin) Tessellator.instance).alternativeTriangulation(true);
            return;
        }
        ((ITessellatorMixin) Tessellator.instance).alternativeTriangulation(false);
    }

    private void reuse(int index) {
        if (reusePreviousStates) {
            ((ITessellatorMixin) Tessellator.instance).alternativeTriangulation(states[index]);
        } else {
            states[index] = ((ITessellatorMixin) Tessellator.instance).alternativeTriangulation();
        }
    }

    @Inject(method = {"renderFaceXNeg"},
            at = @At(value = "HEAD"),
            require = 1)
    private void reuseXNeg(Block p_147798_1_, double p_147798_2_, double p_147798_4_, double p_147798_6_, IIcon p_147798_8_, CallbackInfo ci) {
        reuse(0);
    }

    @Inject(method = {"renderFaceXPos"},
            at = @At(value = "HEAD"),
            require = 1)
    private void reuseXPos(Block p_147798_1_, double p_147798_2_, double p_147798_4_, double p_147798_6_, IIcon p_147798_8_, CallbackInfo ci) {
        reuse(1);
    }

    @Inject(method = {"renderFaceYNeg"},
            at = @At(value = "HEAD"),
            require = 1)
    private void reuseYNeg(Block p_147798_1_, double p_147798_2_, double p_147798_4_, double p_147798_6_, IIcon p_147798_8_, CallbackInfo ci) {
        reuse(2);
    }

    @Inject(method = {"renderFaceYPos"},
            at = @At(value = "HEAD"),
            require = 1)
    private void reuseYPos(Block p_147798_1_, double p_147798_2_, double p_147798_4_, double p_147798_6_, IIcon p_147798_8_, CallbackInfo ci) {
        reuse(3);
    }

    @Inject(method = {"renderFaceZNeg"},
            at = @At(value = "HEAD"),
            require = 1)
    private void reuseZNeg(Block p_147798_1_, double p_147798_2_, double p_147798_4_, double p_147798_6_, IIcon p_147798_8_, CallbackInfo ci) {
        reuse(4);
    }

    @Inject(method = {"renderFaceZPos"},
            at = @At(value = "HEAD"),
            require = 1)
    private void reuseZPos(Block p_147798_1_, double p_147798_2_, double p_147798_4_, double p_147798_6_, IIcon p_147798_8_, CallbackInfo ci) {
        reuse(5);
    }

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
        cir.setReturnValue((((int) sky) & 0xff) | ((((int) block) & 0xff) << 16));
    }

    /**
     * @author embeddedt
     */
    @Redirect(method = {"renderStandardBlockWithAmbientOcclusion", "renderStandardBlockWithAmbientOcclusionPartial"},
               slice = @Slice(from = @At(value = "FIELD",
                                         target = "Lnet/minecraft/client/renderer/RenderBlocks;aoLightValueScratchXYPN:F",
                                         opcode = Opcodes.PUTFIELD,
                                         ordinal = 0),
                              to = @At(value = "FIELD",
                                       target = "Lnet/minecraft/client/renderer/RenderBlocks;aoLightValueScratchXYZNNN:F",
                                       opcode = Opcodes.PUTFIELD,
                                       ordinal = 0)),
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/world/IBlockAccess;getBlock(III)Lnet/minecraft/block/Block;"),
               allow = 8)
    private Block incrementYValue0(IBlockAccess instance, int x, int y, int z) {
        return instance.getBlock(x, y + 1, z);
    }
    /**
     * @author embeddedt
     */
    @Redirect(method = {"renderStandardBlockWithAmbientOcclusion"},
               slice = @Slice(from = @At(value = "FIELD",
                                         target = "Lnet/minecraft/client/renderer/RenderBlocks;aoLightValueScratchYZPP:F",
                                         opcode = Opcodes.PUTFIELD,
                                         ordinal = 0),
                              to = @At(value = "FIELD",
                                       target = "Lnet/minecraft/client/renderer/RenderBlocks;aoLightValueScratchXYZNPN:F",
                                       opcode = Opcodes.PUTFIELD,
                                       ordinal = 0)),
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/world/IBlockAccess;getBlock(III)Lnet/minecraft/block/Block;"),
               allow = 4)
    private Block decrementYValue1(IBlockAccess instance, int x, int y, int z) {
        return instance.getBlock(x, y - 1, z);
    }
    /**
     * @author embeddedt
     */
    @Redirect(method = {"renderStandardBlockWithAmbientOcclusion", "renderStandardBlockWithAmbientOcclusionPartial"},
               slice = @Slice(from = @At(value = "FIELD",
                                         target = "Lnet/minecraft/client/renderer/RenderBlocks;aoBrightnessXZPN:I",
                                         opcode = Opcodes.PUTFIELD,
                                         ordinal = 0),
                              to = @At(value = "FIELD",
                                       target = "Lnet/minecraft/client/renderer/RenderBlocks;aoLightValueScratchXYZNNN:F",
                                       opcode = Opcodes.PUTFIELD,
                                       ordinal = 2)),
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/world/IBlockAccess;getBlock(III)Lnet/minecraft/block/Block;"),
               allow = 8)
    private Block incrementZValue2(IBlockAccess instance, int x, int y, int z) {
        return instance.getBlock(x, y, z + 1);
    }
    /**
     * @author embeddedt
     */
    @Redirect(method = {"renderStandardBlockWithAmbientOcclusion", "renderStandardBlockWithAmbientOcclusionPartial"},
               slice = @Slice(from = @At(value = "FIELD",
                                         target = "Lnet/minecraft/client/renderer/RenderBlocks;aoLightValueScratchYZPP:F",
                                         opcode = Opcodes.PUTFIELD,
                                         ordinal = 1),
                              to = @At(value = "FIELD",
                                       target = "Lnet/minecraft/client/renderer/RenderBlocks;aoLightValueScratchXYZNNP:F",
                                       opcode = Opcodes.PUTFIELD,
                                       ordinal = 2)),
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/world/IBlockAccess;getBlock(III)Lnet/minecraft/block/Block;"),
               allow = 8)
    private Block decrementZValue3(IBlockAccess instance, int x, int y, int z) {
        return instance.getBlock(x, y, z - 1);
    }

    /**
     * @author embeddedt
     */
    @Redirect(method = {"renderStandardBlockWithAmbientOcclusion", "renderStandardBlockWithAmbientOcclusionPartial"},
               slice = @Slice(from = @At(value = "FIELD",
                                         target = "Lnet/minecraft/client/renderer/RenderBlocks;aoLightValueScratchXYNP:F",
                                         opcode = Opcodes.PUTFIELD,
                                         ordinal = 1),
                              to = @At(value = "FIELD",
                                       target = "Lnet/minecraft/client/renderer/RenderBlocks;aoLightValueScratchXYZNNN:F",
                                       opcode = Opcodes.PUTFIELD,
                                       ordinal = 4)),
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/world/IBlockAccess;getBlock(III)Lnet/minecraft/block/Block;"),
               allow = 8)
    private Block incrementXValue4(IBlockAccess instance, int x, int y, int z) {
        return instance.getBlock(x + 1, y, z);
    }
    /**
     * @author embeddedt
     */
    @Redirect(method = {"renderStandardBlockWithAmbientOcclusion", "renderStandardBlockWithAmbientOcclusionPartial"},
              slice = @Slice(from = @At(value = "FIELD",
                                         target = "Lnet/minecraft/client/renderer/RenderBlocks;aoLightValueScratchXYPP:F",
                                         opcode = Opcodes.PUTFIELD,
                                         ordinal = 1),
                              to = @At(value = "FIELD",
                                       target = "Lnet/minecraft/client/renderer/RenderBlocks;aoLightValueScratchXYZPNN:F",
                                       opcode = Opcodes.PUTFIELD,
                                       ordinal = 4)),
              at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/world/IBlockAccess;getBlock(III)Lnet/minecraft/block/Block;"),
              allow = 8)
    private Block decrementXValue5(IBlockAccess instance, int x, int y, int z) {
        return instance.getBlock(x - 1, y, z);
    }
}
