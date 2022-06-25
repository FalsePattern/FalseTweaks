package com.falsepattern.triangulator.mixin.mixins.client.vanilla.leakfix;

import com.falsepattern.triangulator.mixin.helper.IWorldRendererMixin;
import com.falsepattern.triangulator.mixin.helper.LeakFix;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
@Accessors(fluent = true)
public abstract class WorldRendererMixin implements IWorldRendererMixin {
    @Shadow private int glRenderList;

    @Shadow public int posXClip;
    @Shadow public int posYClip;
    @Shadow public int posZClip;
    @Getter
    private boolean hasRenderList;

    @Redirect(method = "<init>",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/WorldRenderer;setPosition(III)V"),
              require = 1)
    private void resetRenderListBefore(WorldRenderer thiz, int x, int y, int z) {
        if (LeakFix.ENABLED) {
            glRenderList = -1;
            hasRenderList = false;
        }
        thiz.setPosition(x, y, z);
    }

    @Inject(method = "setDontDraw",
            at = @At(value = "HEAD"),
            require = 1)
    private void clearLists(CallbackInfo ci) {
        if (LeakFix.ENABLED) {
            clearList();
        }
    }

    @Override
    public void renderAABB() {
        float extra = 6;
        GL11.glNewList(this.glRenderList + 2, 4864);
        RenderItem.renderAABB(AxisAlignedBB.getBoundingBox(this.posXClip - extra, this.posYClip - extra, this.posZClip - extra, this.posXClip + 16 + extra, this.posYClip + 16 + extra, this.posZClip + 16 + extra));
        GL11.glEndList();
    }

    @Override
    public boolean genList() {
        if (hasRenderList) return false;
        glRenderList = LeakFix.allocateWorldRendererBuffer();
        hasRenderList = true;
        return true;
    }

    @Override
    public boolean clearList() {
        if (!hasRenderList) return false;
        hasRenderList = false;
        LeakFix.releaseWorldRendererBuffer(glRenderList);
        glRenderList = -1;
        return true;
    }
}
