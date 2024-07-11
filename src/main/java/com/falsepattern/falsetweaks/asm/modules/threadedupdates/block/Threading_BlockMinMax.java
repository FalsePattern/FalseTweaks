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
package com.falsepattern.falsetweaks.asm.modules.threadedupdates.block;

import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.lib.mapping.MappingManager;
import com.falsepattern.lib.mapping.types.MappingType;
import com.falsepattern.lib.mapping.types.NameType;
import com.falsepattern.lib.mapping.types.UniversalField;
import com.falsepattern.lib.turboasm.ClassNodeHandle;
import com.falsepattern.lib.turboasm.TurboClassTransformer;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.FieldNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO ASM Logging
public class Threading_BlockMinMax implements TurboClassTransformer {
    public static final String classInternal = "net/minecraft/block/Block";
    public static final String packageInternal = classInternal.substring(0, classInternal.lastIndexOf('/') + 1);
    public static final List<UniversalField> fieldsToRemove = new ArrayList<>();
    private static final String targetName = classInternal.replace('/', '.');

    static {
        try {
            val blockClass = MappingManager.classForName(NameType.Internal, MappingType.MCP, classInternal);
            val mcpFields = Arrays.asList("minX", "minY", "minZ", "maxX", "maxY", "maxZ");
            for (val mcpField : mcpFields)
                fieldsToRemove.add(blockClass.getField(MappingType.MCP, mcpField));
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String owner() {
        return Tags.MOD_NAME;
    }

    @Override
    public String name() {
        return "Threading_BlockMinMax";
    }

    @Override
    public boolean shouldTransformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        return targetName.equals(className);
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

        for (val field : fieldsToRemove) {
            val mcpName = field.getName(MappingType.MCP);
            if (fieldName.equals(mcpName))
                return mcpName;
            if (fieldName.equals(field.getName(MappingType.Notch)))
                return mcpName;
            if (fieldName.equals(field.getName(MappingType.SRG)))
                return mcpName;
        }

        return null;
    }
}
