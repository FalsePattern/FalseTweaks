package com.falsepattern.animfix.mixin.mixins.client.minecraft;

import com.falsepattern.animfix.AnimationUpdateBatcher;
import com.falsepattern.animfix.interfaces.ITextureMapMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(TextureMap.class)
public abstract class TextureMapMixin implements ITextureMapMixin {
    private static Profiler theProfiler;
    @Shadow
    private int mipmapLevels;
    private AnimationUpdateBatcher batcher;

    @Inject(method = "loadTexture",
            at = @At(value = "HEAD"),
            require = 1)
    private void setupBatcher(CallbackInfo ci) {
        AnimationUpdateBatcher.currentAtlas = (TextureMap) (Object) this;
    }

    @Inject(method = "loadTexture",
            at = @At(value = "RETURN"),
            require = 1)
    private void finishSetup(CallbackInfo ci) {
        AnimationUpdateBatcher.currentAtlas = null;
    }

    @Redirect(method = "loadTextureAtlas",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                       ordinal = 0),
              require = 1)
    private boolean storeAnimatedInBatch(List<TextureAtlasSprite> listAnimatedSprites, Object obj) {
        TextureAtlasSprite sprite = (TextureAtlasSprite) obj;
        boolean ret = listAnimatedSprites.add(sprite);
        AnimationUpdateBatcher.batcher = batcher;
        TextureUtil.uploadTextureMipmap(sprite.getFrameTextureData(0), sprite.getIconWidth(), sprite.getIconHeight(), sprite.getOriginX(), sprite.getOriginY(), false, false);
        AnimationUpdateBatcher.batcher = null;
        return ret;
    }

    @Inject(method = "updateAnimations",
            at = @At(value = "HEAD"),
            require = 1)
    private void beginBatchAnimations(CallbackInfo ci) {
        if (theProfiler == null) {
            theProfiler = Minecraft.getMinecraft().mcProfiler;
        }
        theProfiler.startSection("updateAnimations");
        AnimationUpdateBatcher.batcher = batcher;
    }

    @Inject(method = "updateAnimations",
            at = @At(value = "RETURN"),
            require = 1)
    private void flushBatchAnimations(CallbackInfo ci) {
        AnimationUpdateBatcher.batcher = null;
        if (batcher != null) {
            theProfiler.startSection("uploadBatch");
            batcher.upload();
            theProfiler.endSection();
        }
        theProfiler.endSection();
    }

    @Override
    public void initializeBatcher(int xOffset, int yOffset, int width, int height) {
        batcher = new AnimationUpdateBatcher(xOffset, yOffset, width, height, mipmapLevels);
    }
}
