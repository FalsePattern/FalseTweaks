package com.falsepattern.falsetweaks.mixin.mixins.client.threadedupdates.notfine;

import com.prupe.mcpatcher.ctm.CTMUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Mixin(value = CTMUtils.class,
       remap = false)
public abstract class CTMUtilsMixin {@Unique
    private static final ReentrantLock ft$lock = new ReentrantLock();
    @Unique
    private static final AtomicInteger ft$lockCounter = new AtomicInteger(0);

    @Inject(method = "getBlockIcon(Lnet/minecraft/util/IIcon;Lnet/minecraft/block/Block;Lnet/minecraft/world/IBlockAccess;IIII)Lnet/minecraft/util/IIcon;",
            at = @At("HEAD"),
            require = 1)
    private static void lock1(IIcon icon, Block block, IBlockAccess blockAccess, int x, int y, int z, int face, CallbackInfoReturnable<IIcon> cir) {
        ft$lock();
    }

    @Inject(method = "getBlockIcon(Lnet/minecraft/util/IIcon;Lnet/minecraft/block/Block;II)Lnet/minecraft/util/IIcon;",
            at = @At("HEAD"),
            require = 1)
    private static void lock2(IIcon icon, Block block, int face, int metadata, CallbackInfoReturnable<IIcon> cir) {
        ft$lock();
    }


    @Inject(method = "getBlockIcon(Lnet/minecraft/util/IIcon;Lnet/minecraft/block/Block;Lnet/minecraft/world/IBlockAccess;IIII)Lnet/minecraft/util/IIcon;",
            at = @At("RETURN"),
            require = 1)
    private static void unlock1(IIcon icon, Block block, IBlockAccess blockAccess, int x, int y, int z, int face, CallbackInfoReturnable<IIcon> cir) {
        ft$unlock();
    }

    @Inject(method = "getBlockIcon(Lnet/minecraft/util/IIcon;Lnet/minecraft/block/Block;II)Lnet/minecraft/util/IIcon;",
            at = @At("RETURN"),
            require = 1)
    private static void unlock2(IIcon icon, Block block, int face, int metadata, CallbackInfoReturnable<IIcon> cir) {
        ft$unlock();
    }

    @Unique
    private static void ft$lock() {
        if (!ft$lock.isHeldByCurrentThread()) {
            while (!ft$lock.tryLock()) {
                Thread.yield();
            }
        }
        ft$lockCounter.incrementAndGet();
    }

    @Unique
    private static void ft$unlock() {
        if (ft$lockCounter.decrementAndGet() == 0) {
            ft$lock.unlock();
        }
    }
}
