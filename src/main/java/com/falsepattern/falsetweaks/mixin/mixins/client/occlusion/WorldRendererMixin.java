package com.falsepattern.falsetweaks.mixin.mixins.client.occlusion;

import com.falsepattern.falsetweaks.modules.occlusion.IWorldRenderer;
import com.falsepattern.falsetweaks.modules.occlusion.OcclusionWorker;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.shader.TesselatorVertexState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraft.world.chunk.EmptyChunk;

import java.util.List;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin implements IWorldRenderer {
    @Shadow public boolean isWaitingOnOcclusionQuery;

    @Shadow public World worldObj;

    @Shadow public int posX;

    @Shadow public int posZ;

    @Shadow public List tileEntityRenderers;

    @Shadow private List tileEntities;

    @Shadow public boolean needsUpdate;

    @Shadow public boolean isInitialized;

    @Shadow private int bytesDrawn;

    @Shadow private TesselatorVertexState vertexState;

    private boolean ft$isInUpdateList;
    private boolean ft$isFrustumCheckPending;

    private OcclusionWorker.CullInfo ft$cullInfo;

    @Inject(method = "<init>*",
            at = @At("RETURN"),
            require = 1)
    private void init(CallbackInfo ci) {
        this.ft$cullInfo = new OcclusionWorker.CullInfo();
    }

    @Inject(method = "markDirty",
            at = @At("TAIL"),
            require = 1)
    private void resetOcclusionFlag(CallbackInfo ci) {
        this.isWaitingOnOcclusionQuery = false;
    }

    @Inject(method = "updateRenderer",
            at = @At(value = "FIELD",
                     opcode = Opcodes.PUTSTATIC,
                     target = "Lnet/minecraft/world/chunk/Chunk;isLit:Z",
                     ordinal = 0),
            require = 1,
            cancellable = true)
    private void bailOnEmptyChunk(EntityLivingBase view, CallbackInfo ci) {
        if(worldObj.getChunkFromBlockCoords(posX, posZ) instanceof EmptyChunk) {
            if (tileEntityRenderers.size() > 0) {
                tileEntities.removeAll(tileEntityRenderers);
                tileEntityRenderers.clear();
            }
            needsUpdate = true;
            isInitialized = false;
            bytesDrawn = 0;
            vertexState = null;
            ci.cancel();
        }
    }

    @Override
    public boolean ft$isInUpdateList() {
        return ft$isInUpdateList;
    }

    @Override
    public void ft$setInUpdateList(boolean b) {
        ft$isInUpdateList = b;
    }

    @Override
    public OcclusionWorker.CullInfo ft$getCullInfo() {
        return ft$cullInfo;
    }
}
