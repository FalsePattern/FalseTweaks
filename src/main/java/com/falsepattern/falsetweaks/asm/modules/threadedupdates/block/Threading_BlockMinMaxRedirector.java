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
package com.falsepattern.falsetweaks.asm.modules.threadedupdates.block;

import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.lib.turboasm.ClassNodeHandle;
import com.falsepattern.lib.turboasm.TurboClassTransformer;
import lombok.val;
import lombok.var;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// TODO ASM Logging
public class Threading_BlockMinMaxRedirector implements TurboClassTransformer {
    /**
     * All classes in net.minecraft.block.* are the block subclasses save for these.
     */
    private static final List<String> VanillaBlockExclusions = Arrays.asList("net/minecraft/block/IGrowable",
                                                                             "net/minecraft/block/ITileEntityProvider",
                                                                             "net/minecraft/block/BlockEventData",
                                                                             "net/minecraft/block/BlockSourceImpl",
                                                                             "net/minecraft/block/material/");

    private static final Set<String> moddedBlockSubclasses = Collections.newSetFromMap(new ConcurrentHashMap<>());
    // Block owners we *shouldn't* redirect because they shadow one of our fields
    private static final Set<String> blockOwnerExclusions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private boolean isVanillaBlockSubclass(String className) {
        // Special case for the regular "Block.class" if we're in notch mappings for some reason?
        if ("aji".equals(className)) {
            return true;
        }

        if (!className.startsWith(Threading_BlockMinMax.packageInternal)) {
            return false;
        }
        for (String exclusion : VanillaBlockExclusions) {
            if (className.startsWith(exclusion)) {
                return false;
            }
        }
        return true;
    }

    private boolean isBlockSubclass(String className) {
        return isVanillaBlockSubclass(className) || moddedBlockSubclasses.contains(className);
    }

    @Override
    public String owner() {
        return Tags.MOD_NAME;
    }

    @Override
    public String name() {
        return "Threading_BlockMinMaxRedirector";
    }

    @Override
    public boolean shouldTransformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        return true;
    }

    @Override
    public boolean transformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        val cn = classNode.getNode();
        if (cn == null) {
            return false;
        }

        // Track subclasses of Block
        if (!isVanillaBlockSubclass(cn.name) && isBlockSubclass(cn.superName)) {
            moddedBlockSubclasses.add(cn.name);
        }

        if (moddedBlockSubclasses.contains(cn.name)) {
            var doWeShadow = false;
            if (blockOwnerExclusions.contains(cn.superName)) {
                doWeShadow = true;
            } else {
                doWeShadow = cn.fields.stream()
                                      .anyMatch(Threading_BlockMinMax::shouldRemoveField);
            }
            if (doWeShadow) {
                blockOwnerExclusions.add(cn.name);
            }
        }

        boolean changed = false;
        for (MethodNode mn : cn.methods) {
            val insnList = mn.instructions.iterator();
            while (insnList.hasNext()) {
                val node = insnList.next();

                // We're looking for a get/ put field instruction
                if (!(node instanceof FieldInsnNode)) {
                    continue;
                }
                val opcode = node.getOpcode();
                if (opcode != Opcodes.GETFIELD && opcode != Opcodes.PUTFIELD) {
                    continue;
                }
                val fNode = (FieldInsnNode) node;

                // Ensure that it deals with a primitive double
                if (!"D".equals(fNode.desc)) {
                    continue;
                }

                // And make sure that this is a sub-class of a block which has not been excluded
                if (!isBlockSubclass(fNode.owner) || blockOwnerExclusions.contains(fNode.owner)) {
                    continue;
                }

                // Now try to find a field to target
                val fieldToRedirect = Threading_BlockMinMax.tryMapFieldNameToMCP(fNode.name);
                if (fieldToRedirect == null) {
                    continue;
                }

                // Replace the instruction with our redirect
                val replacementMethod = new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                                                           Threading_BlockMinMax.classInternal,
                                                           "ft$" + fieldToRedirect,
                                                           opcode == Opcodes.GETFIELD ? "()D" : "(D)V",
                                                           false);
                insnList.set(replacementMethod);
                changed = true;
            }
        }
        return changed;
    }
}
