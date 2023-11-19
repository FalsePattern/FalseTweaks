/*
 * This file is part of FalseTweaks.
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

import com.falsepattern.lib.asm.IClassNodeTransformer;
import lombok.val;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import cpw.mods.fml.client.FMLClientHandler;

/**
 * OptiFine does some weird optimizations that conflict with the occlusion code.
 * So I just remove them :3
 */
public class RenderGlobalDeOptimizer implements IClassNodeTransformer {
    private static final String OWNER_INTERNAL_NAME = "net/minecraft/client/renderer/RenderGlobal";
    private static final String BAD_FIELD_NAME = "t";
    private static final String BAD_FIELD_DESC = "LCompactArrayList;";
    private static final String BAD_METHOD_OWNER = "CompactArrayList";
    private static final String TARGET_FIELD_NAME = "field_72767_j";
    private static final String TARGET_FIELD_DESC = "Ljava/util/List;";
    private static final String TARGET_METHOD_OWNER = "java/util/List";

    @Override
    public String getName() {
        return "RenderGlobalDeOptimizer";
    }

    @Override
    public boolean shouldTransform(ClassNode cn, String transformedName, boolean obfuscated) {
        return "net.minecraft.client.renderer.RenderGlobal".equals(transformedName) &&
               FMLClientHandler.instance().hasOptifine();
    }

    @Override
    public void transform(ClassNode cn, String transformedName, boolean obfuscated) {
        for (val method : cn.methods) {
            if (method.name.equals("<init>") || method.name.equals("<clinit>")) {
                continue;
            }
            val insnList = method.instructions.iterator();
            while (insnList.hasNext()) {
                val insn = insnList.next();
                if (insn instanceof FieldInsnNode) {
                    val field = (FieldInsnNode) insn;
                    if (OWNER_INTERNAL_NAME.equals(field.owner) && BAD_FIELD_NAME.equals(field.name) &&
                        BAD_FIELD_DESC.equals(field.desc)) {
                        field.name = TARGET_FIELD_NAME;
                        field.desc = TARGET_FIELD_DESC;
                    }
                } else if (insn instanceof MethodInsnNode) {
                    val mInsn = (MethodInsnNode) insn;
                    if (BAD_METHOD_OWNER.equals(mInsn.owner)) {
                        mInsn.owner = TARGET_METHOD_OWNER;
                    }
                } else if (insn instanceof TypeInsnNode) {
                    val tInsn = (TypeInsnNode) insn;
                    if (BAD_FIELD_DESC.equals(tInsn.desc)) {
                        tInsn.desc = TARGET_FIELD_DESC;
                    }
                }
            }
        }
    }
}
