
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
import com.falsepattern.lib.mapping.MappingManager;
import com.falsepattern.lib.mapping.types.MappingType;
import com.falsepattern.lib.mapping.types.NameType;
import com.falsepattern.lib.mapping.types.UniversalClass;
import com.falsepattern.lib.mapping.types.UniversalField;
import com.falsepattern.lib.turboasm.ClassNodeHandle;
import com.falsepattern.lib.turboasm.TurboClassTransformer;
import lombok.NoArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;

// TODO Logging
@NoArgsConstructor
public final class Threading_GameSettings implements TurboClassTransformer {
    static final UniversalClass GAME_SETTINGS_CLASS;
    static final UniversalField FANCY_GRAPHICS_FIELD;

    static {
        try {
            GAME_SETTINGS_CLASS = MappingManager.classForName(NameType.Regular,
                                                              MappingType.MCP,
                                                              "net.minecraft.client.settings.GameSettings");
            FANCY_GRAPHICS_FIELD = GAME_SETTINGS_CLASS.getField(MappingType.MCP, "fancyGraphics");
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new AssertionError("Woe be upon ye traveler!", e);
        }
    }


    @Override
    public String owner() {
        return Tags.MOD_NAME;
    }

    @Override
    public String name() {
        return "Threading_GameSettings";
    }

    @Override
    public boolean shouldTransformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        return GAME_SETTINGS_CLASS.regularName.srg.equals(className);
    }

    @Override
    public boolean transformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        val cn = classNode.getNode();
        if (cn == null)
            return false;
        val iter = cn.fields.iterator();
        boolean changed = false;
        while (iter.hasNext()) {
            val field = iter.next();
            if (shouldRemoveField(field)) {
                iter.remove();
                changed = true;
            }
        }
        return changed;
    }

    static boolean isTargetOwner(FieldInsnNode fieldInst) {
        if (fieldInst == null)
            return false;
        val owner = fieldInst.owner;
        if (owner == null)
            return false;

        if (owner.equals(GAME_SETTINGS_CLASS.getName(NameType.Internal, MappingType.Notch)))
            return true;
        if (owner.equals(GAME_SETTINGS_CLASS.getName(NameType.Internal, MappingType.SRG)))
            return true;
        return owner.equals(GAME_SETTINGS_CLASS.getName(NameType.Internal, MappingType.MCP));
    }

    static boolean shouldRemoveField(FieldNode fieldNode) {
        return tryMapFieldNodeToMCP(fieldNode) != null;
    }

    static String tryMapFieldNodeToMCP(FieldNode fieldNode) {
        if (fieldNode == null)
            return null;
        return tryMapFieldNameToMCP(fieldNode.name);
    }

    static String tryMapFieldNameToMCP(String fieldName) {
        if (fieldName == null)
            return null;

        val mcpName = FANCY_GRAPHICS_FIELD.getName(MappingType.MCP);
        if (fieldName.equals(mcpName))
            return mcpName;
        if (fieldName.equals(FANCY_GRAPHICS_FIELD.getName(MappingType.Notch)))
            return mcpName;
        if (fieldName.equals(FANCY_GRAPHICS_FIELD.getName(MappingType.SRG)))
            return mcpName;

        return null;
    }
}
