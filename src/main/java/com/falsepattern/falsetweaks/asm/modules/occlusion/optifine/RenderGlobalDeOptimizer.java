/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2024 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalseTweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FalseTweaks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalseTweaks. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.falsetweaks.asm.modules.occlusion.optifine;

import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.lib.turboasm.ClassNodeHandle;
import com.falsepattern.lib.turboasm.TurboClassTransformer;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import net.minecraft.launchwrapper.Launch;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.Side;

/**
 * OptiFine does some weird optimizations that conflict with the occlusion code.
 * So I just remove them :3
 */
public class RenderGlobalDeOptimizer implements TurboClassTransformer {
    private static final String OWNER_INTERNAL_NAME = "net/minecraft/client/renderer/RenderGlobal";
    private static final String BAD_FIELD_NAME = "t";
    private static final String BAD_FIELD_DESC = "LCompactArrayList;";
    private static final String BAD_METHOD_OWNER = "CompactArrayList";
    private static final String TARGET_FIELD_NAME = "field_72767_j";
    private static final String TARGET_FIELD_DESC = "Ljava/util/List;";
    private static final String TARGET_METHOD_OWNER = "java/util/List";


    @Override
    public String owner() {
        return Tags.MOD_NAME;
    }

    @Override
    public String name() {
        return "RenderGlobalDeOptimizer";
    }

    @Override
    public boolean shouldTransformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        if (!"net.minecraft.client.renderer.RenderGlobal".equals(className)) {
            return false;
        }
        return LazyOptiFineCheck.hasOptiFine();
    }

    private static class LazyOptiFineCheck {
        private static Boolean optifineDetected = null;
        private static boolean hasOptiFine() {
            Boolean detected = optifineDetected;
            if (detected == null) {
                if (FMLLaunchHandler.side().isClient()) {
                    try {
                        //We might be too early but let's try the standard way
                        detected = FMLClientHandler.instance().hasOptifine();
                    } catch (Throwable ignored) {
                        //Ok, we'll do it manually then
                        try {
                            ClassLoader cl;
                            cl = Loader.instance().getModClassLoader();
                            if (cl == null) {
                                cl = Launch.classLoader;
                            }
                            Class.forName("Config", false, cl);
                            detected = true;
                        } catch (Throwable ignored1) {
                            //99.9% sure that optifine is not present
                            detected = false;
                        }
                    }
                } else {
                    //server shouldn't have OF
                    detected = false;
                }
                optifineDetected = detected;
            }
            return detected;
        }
    }

    @Override
    public boolean transformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        val cn = classNode.getNode();
        if (cn == null)
            return false;
        boolean modified = false;
        for (val method : cn.methods) {
            if (method.name.equals("<init>") || method.name.equals("<clinit>")) {
                continue;
            }
            val insnList = method.instructions.iterator();
            while (insnList.hasNext()) {
                val insn = insnList.next();
                if (insn instanceof FieldInsnNode) {
                    val field = (FieldInsnNode) insn;
                    if (OWNER_INTERNAL_NAME.equals(field.owner) && BAD_FIELD_NAME.equals(field.name) && BAD_FIELD_DESC.equals(field.desc)) {
                        field.name = TARGET_FIELD_NAME;
                        field.desc = TARGET_FIELD_DESC;
                        modified = true;
                    }
                } else if (insn instanceof MethodInsnNode) {
                    val mInsn = (MethodInsnNode) insn;
                    if (BAD_METHOD_OWNER.equals(mInsn.owner)) {
                        mInsn.owner = TARGET_METHOD_OWNER;
                        modified = true;
                    }
                } else if (insn instanceof TypeInsnNode) {
                    val tInsn = (TypeInsnNode) insn;
                    if (BAD_FIELD_DESC.equals(tInsn.desc)) {
                        tInsn.desc = TARGET_FIELD_DESC;
                        modified = true;
                    }
                }
            }
        }
        return modified;
    }
}
