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

import com.falsepattern.falsetweaks.api.threading.ThreadSafeBlockRenderer;
import com.falsepattern.falsetweaks.config.ThreadingConfig;
import com.falsepattern.lib.asm.IClassNodeTransformer;
import lombok.val;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Threading_ThreadSafeBlockRendererInjector implements IClassNodeTransformer {
    private static final Set<String> CLASS_NAMES = new HashSet<>();
    private static final Set<String> INTERNAL_NAMES = new HashSet<>();
    private static final Map<String, Handle> INITIALIZERS = new HashMap<>();
    private static final Map<String, String> SUPPLIERS = new HashMap<>();
    private static final String TSBR_InternalName = "com/falsepattern/falsetweaks/api/threading/ThreadSafeBlockRenderer";
    private static final String ISBR_InternalName = "cpw/mods/fml/client/registry/ISimpleBlockRenderingHandler";
    private static final Handle LAMBDA_META_FACTORY = new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
                                                                 "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;");

    static {
        for (val entry: ThreadingConfig.THREAD_SAFE_ISBRHS) {
            val parts = entry.split(":");
            val className = parts[0];
            val internalName = className.replace('.', '/');
            CLASS_NAMES.add(className);
            INTERNAL_NAMES.add(internalName);
            val generator = parts[1];
            if ("safe".equals(generator))
                continue;
            if (generator.contains("!")) {
                Handle creatorHandle;
                if ("default!".equals(generator)) {
                    creatorHandle = new Handle(Opcodes.H_NEWINVOKESPECIAL, internalName, "<init>", "()V");
                } else {
                    val genParts = generator.split("!");
                    val genInternalName = internalName.replace('.', '/');
                    val genMethodName = genParts[1];
                    creatorHandle = new Handle(Opcodes.H_INVOKESTATIC, genInternalName, genMethodName, "()L" + internalName + ";");
                }
                INITIALIZERS.put(internalName, creatorHandle);
            } else {
                SUPPLIERS.put(internalName, generator);
            }
        }
    }
    @Override
    public String getName() {
        return "Threading_ThreadSafeBlockRendererInjector";
    }

    @Override
    public boolean shouldTransform(ClassNode cn, String transformedName, boolean obfuscated) {
        return CLASS_NAMES.contains(transformedName);
    }

    private void injectInstanceCreation(MethodNode method, String internalName) {
        val insnList = method.instructions.iterator();
        insnList.add(new InvokeDynamicInsnNode("get", "()Ljava/util/function/Supplier;",
                                               LAMBDA_META_FACTORY,
                                               Type.getType("()Ljava/lang/Object;"),
                                               INITIALIZERS.get(internalName),
                                               Type.getType("()L" + internalName + ";")));
        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/ThreadLocal", "withInitial", "(Ljava/util/function/Supplier;)Ljava/lang/ThreadLocal;", false));
        insnList.add(new FieldInsnNode(Opcodes.PUTSTATIC, internalName, "ft$tlInjected", "Ljava/lang/ThreadLocal;"));
        if (method.maxStack == 0) {
            method.maxStack = 1;
        }
    }

    @Override
    public void transform(ClassNode cn, String transformedName, boolean obfuscated) {
        if (cn.interfaces.contains(TSBR_InternalName))
            return; // Already implemented, skip

        val internalName = transformedName.replace('.', '/');
        cn.interfaces.add(TSBR_InternalName);
        if (INITIALIZERS.containsKey(internalName)) {
            cn.innerClasses.add(new InnerClassNode("java/lang/invoke/MethodHandles$Lookup", "java/lang/invoke/MethodHandles", "Lookup", Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL));
            cn.fields.add(new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "ft$tlInjected", "Ljava/lang/ThreadLocal;", null, null));
            boolean staticInitializedFound = false;
            for (val method : cn.methods) {
                if (!"<clinit>".equals(method.name))
                    continue;
                staticInitializedFound = true;
                injectInstanceCreation(method, internalName);
            }
            if (!staticInitializedFound) {
                val clinit = new MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
                cn.methods.add(clinit);
                injectInstanceCreation(clinit, internalName);
                clinit.instructions.add(new InsnNode(Opcodes.RETURN));
            }
        }
        val getter = new MethodNode(Opcodes.ACC_PUBLIC, "forCurrentThread", "()L" + ISBR_InternalName + ";", null, null);
        cn.methods.add(getter);
        val insnList = getter.instructions;
        if (SUPPLIERS.containsKey(internalName)) {
            val supplier = SUPPLIERS.get(internalName);
            val parts = supplier.split("\\?");
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, parts[0], parts[1], "()L" + internalName + ";", false));
        } else if (INITIALIZERS.containsKey(internalName)) {
            insnList.add(new FieldInsnNode(Opcodes.GETSTATIC, internalName, "ft$tlInjected", "Ljava/lang/ThreadLocal;"));
            insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/ThreadLocal", "get", "()Ljava/lang/Object;", false));
            insnList.add(new TypeInsnNode(Opcodes.CHECKCAST, ISBR_InternalName));
        } else {
            insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
        }
        insnList.add(new InsnNode(Opcodes.ARETURN));
        getter.maxStack = 1;
        getter.maxLocals = 1;
    }
}
