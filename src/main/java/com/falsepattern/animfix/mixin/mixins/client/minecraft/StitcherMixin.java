package com.falsepattern.animfix.mixin.mixins.client.minecraft;

import com.falsepattern.animfix.AnimationUpdateBatcher;
import com.falsepattern.animfix.config.AnimConfig;
import com.falsepattern.animfix.interfaces.ITextureMapMixin;
import com.falsepattern.animfix.stitching.TooBigException;
import com.falsepattern.animfix.stitching.TurboStitcher;
import cpw.mods.fml.common.ProgressManager;
import lombok.val;
import net.minecraft.client.renderer.StitcherException;
import net.minecraft.client.renderer.texture.Stitcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Set;

@SuppressWarnings("deprecation")
@Mixin(Stitcher.class)
public abstract class StitcherMixin {
    @Shadow
    @Final
    private List<Stitcher.Slot> stitchSlots;

    @Shadow
    private int currentHeight;
    @Shadow
    private int currentWidth;
    private TurboStitcher masterStitcher;
    private TurboStitcher batchingStitcher;

    @Inject(method = "<init>",
            at = @At(value = "RETURN"),
            require = 1)
    private void initTurbo(int maxWidth, int maxHeight, boolean forcePowerOf2, int maxTileDimension, int mipmapLevelStitcher, CallbackInfo ci) {
        masterStitcher = new TurboStitcher(maxWidth, maxHeight);
        batchingStitcher = new TurboStitcher(maxWidth, maxHeight);
        masterStitcher.addSprite(batchingStitcher);
    }

    @Redirect(method = "addSprite",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"),
              require = 1)
    private boolean hijackAdd(Set<Stitcher.Holder> instance, Object e) {
        val holder = (Stitcher.Holder) e;
        val sprite = holder.getAtlasSprite();
        if ((sprite.hasAnimationMetadata() || sprite.getFrameCount() > 1) && (holder.getWidth() <= AnimConfig.maximumBatchedTextureSize && holder.getHeight() <= AnimConfig.maximumBatchedTextureSize)) {
            batchingStitcher.addSprite(holder);
        } else {
            masterStitcher.addSprite((Stitcher.Holder) e);
        }
        return true;
    }

    @Inject(method = "doStitch",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private void doTurboStitch(CallbackInfo ci) {
        ci.cancel();
        try {
            val bar = ProgressManager.push("Texture stitching", 4);
            bar.step("Stitching animated textures");
            batchingStitcher.stitch();
            bar.step("Stitching master atlas");
            masterStitcher.stitch();
            currentWidth = masterStitcher.width;
            currentHeight = masterStitcher.height;
            bar.step("Extracting stitched textures");
            stitchSlots.clear();
            stitchSlots.addAll(masterStitcher.getSlots());
            bar.step("Initializing animated texture batcher");
            ((ITextureMapMixin) AnimationUpdateBatcher.currentAtlas).initializeBatcher(batchingStitcher.x, batchingStitcher.y, batchingStitcher.width, batchingStitcher.height);
            ProgressManager.pop(bar);
        } catch (TooBigException ignored) {
            throw new StitcherException(null, "Unable to fit all textures into atlas. Maybe try a lower resolution resourcepack?");
        } finally {
            masterStitcher.reset();
            batchingStitcher.reset();
            masterStitcher.addSprite(batchingStitcher);
        }
    }
}
