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

import com.falsepattern.lib.asm.ASMUtil;
import com.falsepattern.lib.asm.IClassNodeTransformer;
import lombok.val;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

// TODO ASM Logging
public class Threading_RenderBlocksASM implements IClassNodeTransformer {
    @Override
    public String getName() {
        return "Threading_RenderBlocksASM";
    }

    @Override
    public boolean shouldTransform(ClassNode cn, String transformedName, boolean obfuscated) {
        return "net.minecraft.client.renderer.RenderBlocks".equals(transformedName);
    }

    @Override
    public void transform(ClassNode cn, String transformedName, boolean obfuscated) {
        transformRenderBlockByRenderType(cn);
    }

    /**
     * CallbackInfoReturnable spam avoidance, we need to cancel but this method is called so frequently that the memory
     * spam is significant.
     */
    private void transformRenderBlockByRenderType(ClassNode cn) {
        val method = ASMUtil.findMethodFromMCP(cn, "renderBlockByRenderType", "(Lnet/minecraft/block/Block;III)Z", false);
        val list = method.instructions.iterator();
        boolean found = false;
        while (list.hasNext()) {
            val insn = list.next();
            if (!(insn instanceof VarInsnNode)) {
                continue;
            }
            val varInsn = (VarInsnNode) insn;
            if (varInsn.getOpcode() != Opcodes.ISTORE || varInsn.var != 5) {
                continue;
            }
            found = true;
            break;
        }
        if (!found) {
            throw new IllegalStateException("Could not find injection point!");
        }

        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
        list.add(new VarInsnNode(Opcodes.ILOAD, 5));
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,
                                    "net/minecraft/client/renderer/RenderBlocks",
                                    "ft$cancelRenderDelegatedToDifferentThread",
                                    "(Lnet/minecraft/block/Block;I)I",
                                    false));
        val lbl = new LabelNode();
        list.add(new InsnNode(Opcodes.DUP));
        list.add(new JumpInsnNode(Opcodes.IFEQ, lbl));
        list.add(new InsnNode(Opcodes.ICONST_1));
        list.add(new InsnNode(Opcodes.ISUB));
        list.add(new InsnNode(Opcodes.IRETURN));
        list.add(lbl);
        list.add(new FrameNode(Opcodes.F_SAME1, 0, null, 1, new Object[]{"I"}));
        list.add(new InsnNode(Opcodes.POP));
    }
}