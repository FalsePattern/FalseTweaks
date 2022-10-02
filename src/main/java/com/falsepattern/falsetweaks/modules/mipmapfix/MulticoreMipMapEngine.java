/*
 * FalseTweaks
 *
 * Copyright (C) 2022 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice, this permission notice and the word "SNEED"
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.falsetweaks.modules.mipmapfix;

import com.falsepattern.falsetweaks.Share;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;

import cpw.mods.fml.common.ProgressManager;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MulticoreMipMapEngine {
    private static final ThreadLocal<MulticoreMipMapEngine> engine = new ThreadLocal<>();
    private final ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), r -> {
        val thread = new Thread(r);
        thread.setName("FalseTweaks mipmap thread");
        return thread;
    });
    private final ConcurrentLinkedDeque<String> completedTextures = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<ReportedException> exceptions = new ConcurrentLinkedDeque<>();
    private final ProgressManager.ProgressBar progressBar;

    private static IllegalStateException fatalError() {
        return new IllegalStateException("Multicore mipmap engine broken! Most likely an incompatibility with some other mod, PLEASE report this on the FalseTweaks github repo!");
    }

    public static void initWorkers(ProgressManager.ProgressBar bar) {
        if (engine.get() != null) {
            throw fatalError();
        }
        engine.set(new MulticoreMipMapEngine(bar));
    }

    private void scheduleToThreadsI(TextureAtlasSprite sprite, int mipMapLevels) {
        service.execute(() -> {
            try {
                sprite.generateMipmaps(mipMapLevels);
                completedTextures.add(sprite.getIconName());
            } catch (Throwable throwable1) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable1, "Applying mipmap");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Sprite being mipmapped");
                crashreportcategory.addCrashSectionCallable("Sprite name", sprite::getIconName);
                crashreportcategory.addCrashSectionCallable("Sprite size", () -> sprite.getIconWidth() + " x " + sprite.getIconHeight());
                crashreportcategory.addCrashSectionCallable("Sprite frames", () -> sprite.getFrameCount() + " frames");
                crashreportcategory.addCrashSection("Mipmap levels", mipMapLevels);
                exceptions.add(new ReportedException(crashreport));
            }
        });
    }

    public static void scheduleToThreads(TextureAtlasSprite sprite, int mipMapLevels) {
        val e = engine.get();
        if (e == null) {
            throw fatalError();
        }
        e.scheduleToThreadsI(sprite, mipMapLevels);
    }

    private void waitForWorkEndI() {
        service.shutdown();
        boolean terminated = false;
        ReportedException firstException = null;
        while (!terminated) {
            try {
                terminated = service.awaitTermination(10, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Share.log.trace(e);
            }
            while (completedTextures.size() > 0) {
                String name = completedTextures.poll();
                if (name != null)
                    progressBar.step(name);
            }
            while (exceptions.size() > 0) {
                val ex = exceptions.poll();
                if (firstException == null) {
                    firstException = ex;
                }
                Share.log.error(ex);
            }
        }
        ProgressManager.pop(progressBar);
        if (firstException != null) {
            throw firstException;
        }
    }

    public static void waitForWorkEnd() {
        val e = engine.get();
        if (e == null) {
            throw fatalError();
        }
        e.waitForWorkEndI();
        engine.set(null);
    }
}
