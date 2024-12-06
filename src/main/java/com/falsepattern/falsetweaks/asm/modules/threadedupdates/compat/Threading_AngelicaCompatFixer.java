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

package com.falsepattern.falsetweaks.asm.modules.threadedupdates.compat;

import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.falsetweaks.asm.modules.threadedupdates.Threading_ThreadSafeBlockRendererInjector;
import com.falsepattern.lib.turboasm.ClassNodeHandle;
import com.falsepattern.lib.turboasm.TurboClassTransformer;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class Threading_AngelicaCompatFixer implements TurboClassTransformer {
    private static final String OPTIONAL_INTERFACE_ANN_DESC = "Lcpw/mods/fml/common/Optional$Interface;";
    private static final String OPTIONAL_METHOD_ANN_DESC = "Lcpw/mods/fml/common/Optional$Method;";
    private static final String ANGELICA_THREAD_SAFE_FACTORY_InternalName = "com/gtnewhorizons/angelica/api/ThreadSafeISBRHFactory";
    private static final String ANGELICA_THREAD_SAFE_FACTORY_ClassName = ANGELICA_THREAD_SAFE_FACTORY_InternalName.replace('/', '.');
    private static final String ANGELICA_FACTORY_METHOD_DESC = "()L" + ANGELICA_THREAD_SAFE_FACTORY_InternalName + ";";
    private static final String THREAD_SAFE_FACTORY_ClassName = Threading_ThreadSafeBlockRendererInjector.THREAD_SAFE_FACTORY_InternalName.replace('/', '.');
    @Override
    public String owner() {
        return Tags.MOD_ID;
    }

    @Override
    public String name() {
        return "Threading_AngelicaCompatFixer";
    }

    @Override
    public boolean shouldTransformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        return true;
    }

    @Override
    public boolean transformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        val node = classNode.getNode();
        if (node == null)
            return false;
        boolean found = false;
        {
            val anns = node.visibleAnnotations;
            if (anns == null)
                return false;
            for (val ann : anns) {
                if (ann.desc.equals(OPTIONAL_INTERFACE_ANN_DESC)) {
                    boolean isFactory = false;
                    String modid = null;
                    Boolean stripRefs = null;
                    val values = ann.values;
                    if (values == null)
                        continue;
                    val iter = values.iterator();
                    while (iter.hasNext()) {
                        val name = (String) iter.next();
                        val value = iter.next();
                        switch (name) {
                            case "iface": {
                                if (ANGELICA_THREAD_SAFE_FACTORY_ClassName.equals(value)) {
                                    isFactory = true;
                                }
                                break;
                            }
                            case "modid": {
                                modid = (String) value;
                                break;
                            }
                            case "striprefs": {
                                stripRefs = (Boolean) value;
                                break;
                            }
                        }
                    }
                    if (!isFactory) {
                        continue;
                    }
                    found = true;
                    ann.values = constructNewValues(modid, stripRefs);
                    break;
                }
            }
        }
        if (!found) {
            return false;
        }
        for (val method: node.methods) {
            if (!(Threading_ThreadSafeBlockRendererInjector.FACTORY_METHOD_NAME.equals(method.name) &&
                  Threading_ThreadSafeBlockRendererInjector.FACTORY_METHOD_DESC.equals(method.desc))) {
                continue;
            }
            val anns = method.visibleAnnotations;
            if (anns == null)
                continue;
            for (val ann: anns) {
                if (ann.desc.equals(OPTIONAL_METHOD_ANN_DESC)) {
                    String modid = null;
                    val values = ann.values;
                    if (values == null)
                        continue;
                    val iter = values.iterator();
                    while (iter.hasNext()) {
                        val name = (String)iter.next();
                        val value = iter.next();
                        if ("modid".equals(name)) {
                            modid = (String) value;
                            break;
                        }
                    }
                    if ("angelica".equals(modid)) {
                        ann.values = new ArrayList<>(Arrays.asList("modid", Tags.MOD_ID));
                    }
                }
            }
        }
        return true;
    }

    private static @NotNull ArrayList<Object> constructNewValues(String modid, Boolean stripRefs) {
        val newValues = new ArrayList<>();
        newValues.add("iface");
        newValues.add(THREAD_SAFE_FACTORY_ClassName);
        newValues.add("modid");
        if ("angelica".equals(modid)) {
            newValues.add(Tags.MOD_ID);
        } else {
            newValues.add(modid);
        }
        if (stripRefs != null) {
            newValues.add("striprefs");
            newValues.add(stripRefs);
        }
        return newValues;
    }
}
