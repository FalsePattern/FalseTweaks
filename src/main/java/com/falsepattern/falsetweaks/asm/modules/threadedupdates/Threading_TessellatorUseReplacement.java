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
package com.falsepattern.falsetweaks.asm.modules.threadedupdates;

import com.falsepattern.falsetweaks.config.ThreadingConfig;
import com.falsepattern.lib.asm.IClassNodeTransformer;
import lombok.val;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// TODO ASM Logging
public class Threading_TessellatorUseReplacement implements IClassNodeTransformer {
    private static final String TARGET_DESC_NOTCH = "Lbmh;";
    private static final String TARGET_DESC_SRG = "Lnet/minecraft/client/renderer/Tessellator;";

    private static final String REPLACEMENT_OWNER = "com/falsepattern/falsetweaks/api/ThreadedChunkUpdates";
    private static final String REPLACEMENT_NAME = "getThreadTessellator";
    private static final String REPLACEMENT_DESC = "()Lnet/minecraft/client/renderer/Tessellator;";

    private static final Set<String> CLASS_NAMES = new HashSet<>();
    private static final List<String> PREFIXES = new ArrayList<>();
    static {
        for (val targetName: ThreadingConfig.TESSELLATOR_USE_REPLACEMENT_TARGETS) {
            if (!targetName.endsWith("*")) {
                CLASS_NAMES.add(targetName);
            } else {
                PREFIXES.add(targetName.substring(0, targetName.length() - 1));
            }
        }
    }

    @Override
    public String getName() {
        return "Threading_TessellatorUseReplacement";
    }

    @Override
    public boolean shouldTransform(ClassNode cn, String transformedName, boolean obfuscated) {
        if (CLASS_NAMES.contains(transformedName))
            return true;
        for (val prefix: PREFIXES) {
            if (transformedName.startsWith(prefix))
                return true;
        }
        return false;
    }

    @Override
    public void transform(ClassNode cn, String transformedName, boolean obfuscated) {
        val methods = cn.methods;
        for (val classMethod : methods) {
            val insnList = classMethod.instructions.iterator();
            while (insnList.hasNext()) {
                val insn = insnList.next();
                if (insn instanceof FieldInsnNode) {
                    val fieldInst = (FieldInsnNode) insn;
                    if (!(TARGET_DESC_NOTCH.equals(fieldInst.desc) || TARGET_DESC_SRG.equals(fieldInst.desc))) {
                        continue;
                    }

                    val opcode = insn.getOpcode();
                    if (opcode == Opcodes.GETFIELD || opcode == Opcodes.GETSTATIC) {
                        insnList.add(new InsnNode(Opcodes.POP));
                        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, REPLACEMENT_OWNER, REPLACEMENT_NAME, REPLACEMENT_DESC, false));
                    }
                }
            }
        }
    }
}
