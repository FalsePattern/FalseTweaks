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
package com.falsepattern.falsetweaks.asm.modules.threadedupdates.settings;

import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.lib.turboasm.ClassNodeHandle;
import com.falsepattern.lib.turboasm.TurboClassTransformer;
import lombok.NoArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import static com.falsepattern.falsetweaks.asm.modules.threadedupdates.settings.Threading_GameSettings.GAME_SETTINGS_CLASS;

// TODO ASM Logging
@NoArgsConstructor
public final class Threading_GameSettingsRedirector implements TurboClassTransformer {

    @Override
    public String owner() {
        return Tags.MOD_NAME;
    }

    @Override
    public String name() {
        return "Threading_GameSettingsRedirector";
    }

    @Override
    public boolean shouldTransformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        return true;
    }

    @Override
    public boolean transformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        val cn = classNode.getNode();
        if (cn == null)
            return false;

        boolean didWork = false;
        for (val mn : cn.methods) {
            val insnList = mn.instructions.iterator();
            while (insnList.hasNext()) {
                val node = insnList.next();

                // We're looking for a get/ put field instruction
                if (!(node instanceof FieldInsnNode))
                    continue;
                val opcode = node.getOpcode();
                if (opcode != Opcodes.GETFIELD && opcode != Opcodes.PUTFIELD)
                    continue;
                val fNode = (FieldInsnNode) node;

                // Ensure that it deals with a primitive boolean
                if (!"Z".equals(fNode.desc))
                    continue;

                // And make sure that this is owned by the settings class
                if (!Threading_GameSettings.isTargetOwner(fNode))
                    continue;

                // Now try to find a field to target
                val fieldToRedirect = Threading_GameSettings.tryMapFieldNameToMCP(fNode.name);
                if (fieldToRedirect == null)
                    continue;

                // Replace the instruction with our redirect
                val replacementMethod = new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                                                           GAME_SETTINGS_CLASS.internalName.srg,
                                                           "ft$" + fieldToRedirect,
                                                           opcode == Opcodes.GETFIELD ? "()Z" : "(Z)V",
                                                           false);
                insnList.set(replacementMethod);

                didWork = true;
            }
        }
        return didWork;
    }
}
