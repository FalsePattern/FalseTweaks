package com.falsepattern.triangulator.mixin.mixins.client;

import com.falsepattern.triangulator.mixin.helper.IQuadComparatorMixin;
import net.minecraft.client.util.QuadComparator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(QuadComparator.class)
public abstract class QuadComparatorMixin implements IQuadComparatorMixin {
    @Shadow private float field_147630_a;

    @Shadow private float field_147628_b;

    @Shadow private float field_147629_c;

    @Shadow private int[] field_147627_d;

    private boolean triMode = false;

    @Inject(method = "compare(Ljava/lang/Integer;Ljava/lang/Integer;)I",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private void triCompare(Integer aObj, Integer bObj, CallbackInfoReturnable<Integer> cir) {
        if (!triMode) return;
        int a = aObj;
        int b = bObj;
        float x = this.field_147630_a;
        float y = this.field_147628_b;
        float z = this.field_147629_c;
        int[] vertexData = this.field_147627_d;
        float a0x = Float.intBitsToFloat(vertexData[a]) - x;
        float a0y = Float.intBitsToFloat(vertexData[a + 1]) - y;
        float a0z = Float.intBitsToFloat(vertexData[a + 2]) - z;
        float a1x = Float.intBitsToFloat(vertexData[a + 8]) - x;
        float a1y = Float.intBitsToFloat(vertexData[a + 9]) - y;
        float a1z = Float.intBitsToFloat(vertexData[a + 10]) - z;
        float a2x = Float.intBitsToFloat(vertexData[a + 16]) - x;
        float a2y = Float.intBitsToFloat(vertexData[a + 17]) - y;
        float a2z = Float.intBitsToFloat(vertexData[a + 18]) - z;
        float b0x = Float.intBitsToFloat(vertexData[b]) - x;
        float b0y = Float.intBitsToFloat(vertexData[b + 1]) - y;
        float b0z = Float.intBitsToFloat(vertexData[b + 2]) - z;
        float b1x = Float.intBitsToFloat(vertexData[b + 8]) - x;
        float b1y = Float.intBitsToFloat(vertexData[b + 9]) - y;
        float b1z = Float.intBitsToFloat(vertexData[b + 10]) - z;
        float b2x = Float.intBitsToFloat(vertexData[b + 16]) - x;
        float b2y = Float.intBitsToFloat(vertexData[b + 17]) - y;
        float b2z = Float.intBitsToFloat(vertexData[b + 18]) - z;
        float axAvg = (a0x + a1x + a2x) / 3f;
        float ayAvg = (a0y + a1y + a2y) / 3f;
        float azAvg = (a0z + a1z + a2z) / 3f;
        float bxAvg = (b0x + b1x + b2x) / 3f;
        float byAvg = (b0y + b1y + b2y) / 3f;
        float bzAvg = (b0z + b1z + b2z) / 3f;
        float aAvg = axAvg * axAvg + ayAvg * ayAvg + azAvg * azAvg;
        float bAvg = bxAvg * bxAvg + byAvg * byAvg + bzAvg * bzAvg;
        cir.setReturnValue(Float.compare(bAvg, aAvg));
    }

    @Override
    public void enableTriMode() {
        triMode = true;
    }
}
