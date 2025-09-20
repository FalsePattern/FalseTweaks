/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2025 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalseTweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
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

import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.falsetweaks.config.ThreadingConfig;
import com.falsepattern.lib.turboasm.ClassNodeHandle;
import com.falsepattern.lib.turboasm.TurboClassTransformer;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

// TODO ASM Logging

public class Threading_ThreadSafeBlockRendererInjector implements TurboClassTransformer {
    public static final String THREAD_SAFE_ANNOTATION_InternalName = "com/falsepattern/falsetweaks/modules/threadedupdates/interop/ThreadSafeISBRH";
    public static final String THREAD_SAFE_ANNOTATION_DESC = "Lcom/falsepattern/falsetweaks/modules/threadedupdates/interop/ThreadSafeISBRH;";
    public static final String THREAD_SAFE_FACTORY_InternalName = "com/falsepattern/falsetweaks/modules/threadedupdates/interop/ThreadSafeISBRHFactory";
    public static final String FACTORY_METHOD_DESC = "()L" + THREAD_SAFE_FACTORY_InternalName + ";";
    public static final String FACTORY_METHOD_NAME = "newInstance";
    private static final Set<String> CLASS_NAMES = new HashSet<>();
    private static final Set<String> INTERNAL_NAMES = new HashSet<>();
    private static final Map<String, Handle> INITIALIZERS = new HashMap<>();
    private static final Map<String, String> SUPPLIERS = new HashMap<>();
    private static final Set<String> FACTORIES = new HashSet<>();
    private static final String TSBR_InternalName = "com/falsepattern/falsetweaks/api/threading/ThreadSafeBlockRenderer";
    private static final String ISBR_InternalName = "cpw/mods/fml/client/registry/ISimpleBlockRenderingHandler";
    private static final Handle LAMBDA_META_FACTORY = new Handle(Opcodes.H_INVOKESTATIC,
                                                                 "java/lang/invoke/LambdaMetafactory",
                                                                 "metafactory",
                                                                 "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;");
    private static final String[] HARDCODED = new String[]{
            "com.carpentersblocks.renderer.BlockHandlerCarpentersBarrier:default!",
            "com.carpentersblocks.renderer.BlockHandlerCarpentersBed:default!",
            "com.carpentersblocks.renderer.BlockHandlerCarpentersBlock:default!",
            "com.carpentersblocks.renderer.BlockHandlerCarpentersButton:default!",
            "com.carpentersblocks.renderer.BlockHandlerCarpentersCollapsibleBlock:default!",
            "com.carpentersblocks.renderer.BlockHandlerCarpentersDaylightSensor:default!",
            "com.carpentersblocks.renderer.BlockHandlerCarpentersDoor:default!",
            "com.carpentersblocks.renderer.BlockHandlerCarpentersFlowerPot:default!",
            "com.carpentersblocks.renderer.BlockHandlerCarpentersGarageDoor:default!",
            "com.carpentersblocks.renderer.BlockHandlerCarpentersGate:default!",
            "com.carpentersblocks.renderer.BlockHandlerCarpentersHatch:default!",
            "com.carpentersblocks.renderer.BlockHandlerCarpentersLadder:default!",
            "com.carpentersblocks.renderer.BlockHandlerCarpentersLever:default!",
            "com.carpentersblocks.renderer.BlockHandlerCarpentersPressurePlate:default!",
            "com.carpentersblocks.renderer.BlockHandlerCarpentersSafe:default!",
            "com.carpentersblocks.renderer.BlockHandlerCarpentersSlope:default!",
            "com.carpentersblocks.renderer.BlockHandlerCarpentersStairs:default!",
            "com.carpentersblocks.renderer.BlockHandlerCarpentersTorch:default!",
            "com.falsepattern.rple.api.client.render.LampRenderer:safe",
            "com.jaquadro.minecraft.storagedrawers.client.renderer.ControllerRenderer:default!",
            "com.jaquadro.minecraft.storagedrawers.client.renderer.DrawersCustomRenderer:default!",
            "com.jaquadro.minecraft.storagedrawers.client.renderer.DrawersRenderer:default!",
            "com.jaquadro.minecraft.storagedrawers.client.renderer.FramingTableRenderer:default!",
            "com.jaquadro.minecraft.storagedrawers.client.renderer.TrimCustomRenderer:default!",
            "net.minecraftforge.fluids.RenderBlockFluid:safe",};
    private static final Method createSupplier;

    static {
        addAll(HARDCODED);
        addAll(ThreadingConfig.THREAD_SAFE_ISBRHS);
    }

    static {
        try {
            createSupplier = Method.getMethod(LegacyBytecodeSupplierFactory.class.getDeclaredMethod("createSupplier",
                                                                                                    String.class,
                                                                                                    String.class,
                                                                                                    String.class,
                                                                                                    int.class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addAll(String... entries) {
        for (val entry : entries) {
            val parts = entry.split(":");
            val className = parts[0];
            val internalName = className.replace('.', '/');
            CLASS_NAMES.add(className);
            INTERNAL_NAMES.add(internalName);
            val generator = parts[1];
            if ("safe".equals(generator)) {
                continue;
            }
            if (generator.contains("!")) {
                Handle creatorHandle;
                if ("default!".equals(generator)) {
                    creatorHandle = new Handle(Opcodes.H_NEWINVOKESPECIAL, internalName, "<init>", "()V");
                } else {
                    val genParts = generator.split("!");
                    val genInternalName = genParts[0].replace('.', '/');
                    val genMethodName = genParts[1];
                    creatorHandle = new Handle(Opcodes.H_INVOKESTATIC,
                                               genInternalName,
                                               genMethodName,
                                               "()L" + internalName + ";");
                }
                INITIALIZERS.put(internalName, creatorHandle);
            } else {
                SUPPLIERS.put(internalName, generator);
            }
        }
    }

    @Override
    public String owner() {
        return Tags.MOD_NAME;
    }

    @Override
    public String name() {
        return "Threading_ThreadSafeBlockRendererInjector";
    }

    @Override
    public boolean shouldTransformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        if (CLASS_NAMES.contains(className)) {
            return true;
        } else {
            val node = classNode.getNode();
            if (node == null) {
                return false;
            }
            val anns = node.visibleAnnotations;
            if (anns == null) {
                return false;
            }
            val internalName = className.replace('.', '/');
            for (val ann : anns) {
                if (THREAD_SAFE_ANNOTATION_DESC.equals(ann.desc)) {
                    boolean perThread = false;
                    val values = ann.values;
                    if (values != null) {
                        val iter = values.iterator();
                        while (iter.hasNext()) {
                            val name = iter.next();
                            val value = iter.next();
                            if ("perThread".equals(name)) {
                                perThread = (Boolean) value;
                            }
                        }
                    }
                    if (perThread) {
                        INITIALIZERS.put(internalName,
                                         new Handle(Opcodes.H_NEWINVOKESPECIAL, internalName, "<init>", "()V"));
                    }
                    return true;
                }
            }
            val ifcs = node.interfaces;
            if (ifcs == null) {
                return false;
            }
            for (val ifc : ifcs) {
                if (THREAD_SAFE_FACTORY_InternalName.equals(ifc)) {
                    FACTORIES.add(className.replace('.', '/'));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean transformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        val cn = classNode.getNode();
        if (cn == null) {
            return false;
        }
        if (cn.interfaces.contains(TSBR_InternalName)) {
            return false; // Already implemented, skip
        }

        val internalName = className.replace('.', '/');
        cn.interfaces.add(TSBR_InternalName);
        val getter = new MethodNode(Opcodes.ACC_PUBLIC,
                                    "forCurrentThread",
                                    "()L" + ISBR_InternalName + ";",
                                    null,
                                    null);
        cn.methods.add(getter);
        val insnList = getter.instructions;
        if (INITIALIZERS.containsKey(internalName)) {
            injectThreadLocal(cn, internalName, true);
            insnList.add(new FieldInsnNode(Opcodes.GETSTATIC,
                                           internalName,
                                           "ft$tlInjected",
                                           "Ljava/lang/ThreadLocal;"));
            insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                                            "java/lang/ThreadLocal",
                                            "get",
                                            "()Ljava/lang/Object;",
                                            false));
            insnList.add(new TypeInsnNode(Opcodes.CHECKCAST, ISBR_InternalName));
            getter.maxStack = 1;
        } else if (FACTORIES.contains(internalName)) {
            injectThreadLocal(cn, internalName, false);
            insnList.add(new FieldInsnNode(Opcodes.GETSTATIC,
                                           internalName,
                                           "ft$tlInjected",
                                           "Ljava/lang/ThreadLocal;"));
            insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                                            "java/lang/ThreadLocal",
                                            "get",
                                            "()Ljava/lang/Object;",
                                            false));
            insnList.add(new InsnNode(Opcodes.DUP));
            val nonNull = new LabelNode();
            insnList.add(new JumpInsnNode(Opcodes.IFNONNULL, nonNull));
            insnList.add(new InsnNode(Opcodes.POP));
            insnList.add(new FieldInsnNode(Opcodes.GETSTATIC,
                                           internalName,
                                           "ft$tlInjected",
                                           "Ljava/lang/ThreadLocal;"));
            insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
            insnList.add(new TypeInsnNode(Opcodes.CHECKCAST, THREAD_SAFE_FACTORY_InternalName));
            insnList.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE,
                                            THREAD_SAFE_FACTORY_InternalName,
                                            FACTORY_METHOD_NAME,
                                            FACTORY_METHOD_DESC,
                                            true));
            insnList.add(new InsnNode(Opcodes.DUP_X1));
            insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                                            "java/lang/ThreadLocal",
                                            "set",
                                            "(Ljava/lang/Object;)V",
                                            false));
            insnList.add(nonNull);
            insnList.add(new FrameNode(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/Object"}));
            insnList.add(new TypeInsnNode(Opcodes.CHECKCAST, ISBR_InternalName));
            getter.maxStack = 3;
        } else if (SUPPLIERS.containsKey(internalName)) {
            val supplier = SUPPLIERS.get(internalName);
            val parts = supplier.split("\\?");
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                            parts[0],
                                            parts[1],
                                            "()L" + internalName + ";",
                                            false));
            getter.maxStack = 1;
        } else {
            insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
            getter.maxStack = 1;
        }
        insnList.add(new InsnNode(Opcodes.ARETURN));
        getter.maxLocals = 1;
        return true;
    }

    private void injectThreadLocal(ClassNode cn, String internalName, boolean withInitial) {
        if (withInitial) {
            cn.innerClasses.add(new InnerClassNode("java/lang/invoke/MethodHandles$Lookup",
                                                   "java/lang/invoke/MethodHandles",
                                                   "Lookup",
                                                   Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL));
        }
        cn.fields.add(new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
                                    "ft$tlInjected",
                                    "Ljava/lang/ThreadLocal;",
                                    null,
                                    null));
        boolean staticInitializedFound = false;
        for (val method : cn.methods) {
            if (!"<clinit>".equals(method.name)) {
                continue;
            }
            staticInitializedFound = true;
            injectThreadLocalCreation(method, internalName, withInitial, cn.version);
        }
        if (!staticInitializedFound) {
            val clinit = new MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            clinit.instructions.add(new FrameNode(Opcodes.F_FULL, 0, new Object[0], 0, new Object[0]));
            cn.methods.add(clinit);
            injectThreadLocalCreation(clinit, internalName, withInitial, cn.version);
            clinit.instructions.add(new InsnNode(Opcodes.RETURN));
        }
    }

    private void injectThreadLocalCreation(MethodNode method, String internalName, boolean withInitial, int version) {
        val insnList = method.instructions.iterator();
        if (withInitial) {
            if (version >= Opcodes.V1_8) {
                insnList.add(new InvokeDynamicInsnNode("get",
                                                       "()Ljava/util/function/Supplier;",
                                                       LAMBDA_META_FACTORY,
                                                       Type.getType("()Ljava/lang/Object;"),
                                                       INITIALIZERS.get(internalName),
                                                       Type.getType("()L" + internalName + ";")));
            } else {
                val initializer = INITIALIZERS.get(internalName);
                insnList.add(new LdcInsnNode(initializer.getOwner()));
                insnList.add(new LdcInsnNode(initializer.getName()));
                insnList.add(new LdcInsnNode(initializer.getDesc()));
                insnList.add(new LdcInsnNode(initializer.getTag()));
                insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                                Type.getInternalName(LegacyBytecodeSupplierFactory.class),
                                                createSupplier.getName(),
                                                createSupplier.getDescriptor(),
                                                false));
            }
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                            "java/lang/ThreadLocal",
                                            "withInitial",
                                            "(Ljava/util/function/Supplier;)Ljava/lang/ThreadLocal;",
                                            false));
        } else {
            insnList.add(new TypeInsnNode(Opcodes.NEW, "java/lang/ThreadLocal"));
            insnList.add(new InsnNode(Opcodes.DUP));
            insnList.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/ThreadLocal", "<init>", "()V", false));
        }
        insnList.add(new FieldInsnNode(Opcodes.PUTSTATIC, internalName, "ft$tlInjected", "Ljava/lang/ThreadLocal;"));
        if (method.maxStack == 0) {
            method.maxStack = 1;
        }
    }

    public static class LegacyBytecodeSupplierFactory {
        public static Supplier<Object> createSupplier(String owner, String name, String desc, int tag)
                throws ClassNotFoundException {
            val klass = Class.forName(owner.replace('/', '.'));
            val methodType = Type.getMethodType(desc);
            val args = methodType.getArgumentTypes();
            if (tag == Opcodes.H_INVOKESTATIC) {
                val methods = klass.getDeclaredMethods();
                outer:
                for (val method : methods) {
                    if (!Objects.equals(name, method.getName())) {
                        continue;
                    }
                    val params = method.getParameters();
                    if (params.length != args.length) {
                        continue;
                    }
                    if (!Objects.equals(methodType.getReturnType(), Type.getType(method.getReturnType()))) {
                        continue;
                    }
                    for (int i = 0; i < params.length; i++) {
                        if (!Objects.equals(args[i], Type.getType(params[i].getType()))) {
                            continue outer;
                        }
                    }
                    method.setAccessible(true);
                    return () -> {
                        try {
                            return method.invoke(null);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    };
                }
                throw new IllegalArgumentException("Could not find target method " + owner + ":" + name + ":" + desc);
            } else if (tag == Opcodes.H_NEWINVOKESPECIAL) {
                val constructors = klass.getDeclaredConstructors();
                outer:
                for (val method : constructors) {
                    val params = method.getParameters();
                    if (params.length != args.length) {
                        continue;
                    }
                    for (int i = 0; i < params.length; i++) {
                        if (!Objects.equals(args[i], Type.getType(params[i].getType()))) {
                            continue outer;
                        }
                    }
                    method.setAccessible(true);
                    return () -> {
                        try {
                            return method.newInstance();
                        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                            throw new RuntimeException(e);
                        }
                    };
                }
                throw new IllegalArgumentException("Could not find target constructor " +
                                                   owner +
                                                   ":" +
                                                   name +
                                                   ":" +
                                                   desc);
            } else {
                throw new IllegalArgumentException("Unknown call tag: " + tag);
            }
        }
    }
}
