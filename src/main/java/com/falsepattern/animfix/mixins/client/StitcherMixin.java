package com.falsepattern.animfix.mixins.client;

import com.falsepattern.animfix.AnimationUpdateBatcher;
import com.falsepattern.animfix.Config;
import com.falsepattern.animfix.MegaTexture;
import com.falsepattern.animfix.interfaces.IRecursiveStitcher;
import com.falsepattern.animfix.interfaces.IStitcherSlotMixin;
import com.falsepattern.animfix.interfaces.ITextureMapMixin;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.List;

@Mixin(Stitcher.class)
public abstract class StitcherMixin implements IRecursiveStitcher {
    private boolean skipRecursion;
    private Set<Stitcher.Holder> animatedHolders;
    private List<Stitcher.Slot> animatedSlots;
    private TextureAtlasSprite megaTexture;

    @Shadow @Final private Set<Stitcher.Holder> setStitchHolders;

    @Shadow @Final private boolean forcePowerOf2;

    @Shadow @Final private int maxWidth;

    @Shadow @Final private int maxHeight;

    @Shadow @Final private int maxTileDimension;

    @Shadow @Final private int mipmapLevelStitcher;

    @Shadow public abstract void addSprite(TextureAtlasSprite p_110934_1_);

    @Shadow @Final private List<Stitcher.Slot> stitchSlots;

    @Override
    public void doNotRecurse() {
        skipRecursion = true;
    }

    @Override
    public List<Stitcher.Slot> getSlots() {
        return stitchSlots;
    }

    @Inject(method = "doStitch",
            at = @At(value = "HEAD"),
            require = 1)
    private void doStitch_0(CallbackInfo ci) {
        if (!skipRecursion) {
            animatedHolders = new HashSet<>();
            int maxSize = Config.maximumBatchedTextureSize;

            //Extract animated sprites that are smaller than the max size
            for (Stitcher.Holder setStitchHolder : setStitchHolders) {
                TextureAtlasSprite sprite = setStitchHolder.getAtlasSprite();
                if (sprite.getIconWidth() > maxSize || sprite.getIconHeight() > maxSize) continue;
                if (setStitchHolder.getAtlasSprite().hasAnimationMetadata()) {
                    animatedHolders.add(setStitchHolder);
                }
            }

            //Bailout if there aren't any animated textures
            if (animatedHolders.size() == 0) {
                skipRecursion = true;
            } else {

                setStitchHolders.removeAll(animatedHolders);

                //Put animated textures into a "block"
                Stitcher recursiveStitcher = new Stitcher(maxWidth, maxHeight, forcePowerOf2, maxTileDimension, mipmapLevelStitcher);
                ((IRecursiveStitcher) recursiveStitcher).doNotRecurse();
                for (Stitcher.Holder holder : animatedHolders) {
                    recursiveStitcher.addSprite(holder.getAtlasSprite());
                }
                recursiveStitcher.doStitch();

                //Replace the textures with a placeholder in the atlas
                animatedSlots = ((IRecursiveStitcher) recursiveStitcher).getSlots();
                megaTexture = new MegaTexture();
                megaTexture.setIconWidth(recursiveStitcher.getCurrentWidth());
                megaTexture.setIconHeight(recursiveStitcher.getCurrentHeight());
                addSprite(megaTexture);
            }
        }
    }

    @Inject(method = "doStitch",
            at = @At(value = "RETURN"),
            require = 1)
    private void doStitch_1(CallbackInfo ci) {
        if (!skipRecursion) {
            //Find our placeholder in the atlas
            Stitcher.Slot megaTextureSlot = null;
            for (Stitcher.Slot slot : stitchSlots) {
                megaTextureSlot = removeMegaSlotRecursive(slot);
                if (megaTextureSlot != null) break;
            }
            if (megaTextureSlot == null) {
                throw new IllegalStateException("Failed to extract animated texture stitching slot!");
            }

            //Clear the texture from the placeholder, and populate it with the animated textures
            Stitcher.Holder megaHolder = megaTextureSlot.getStitchHolder();
            setStitchHolders.remove(megaHolder);
            ((IStitcherSlotMixin) megaTextureSlot).insertHolder(null);
            for (Stitcher.Slot animatedSlot: animatedSlots) {
                ((IStitcherSlotMixin) megaTextureSlot).insertSlot(animatedSlot);
            }
            ((ITextureMapMixin) AnimationUpdateBatcher.currentAtlas).initializeBatcher(megaTextureSlot.getOriginX(), megaTextureSlot.getOriginY(), megaHolder.getWidth(), megaHolder.getHeight());
        }
        skipRecursion = false;
        animatedHolders = null;
        animatedSlots = null;
        megaTexture = null;
    }

    private Stitcher.Slot removeMegaSlotRecursive(Stitcher.Slot slot) {
        Stitcher.Holder holder = slot.getStitchHolder();
        if (holder == null) {
            List<Stitcher.Slot> slots = new ArrayList<>();
            slot.getAllStitchSlots(slots);
            for (Stitcher.Slot slot2 : slots) {
                Stitcher.Slot result = removeMegaSlotRecursive(slot2);
                if (result != null) return result;
            }
        } else if (holder.getAtlasSprite() == megaTexture) {
            return slot;
        }
        return null;
    }

}
