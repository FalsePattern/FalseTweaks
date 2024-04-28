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

package com.falsepattern.falsetweaks.modules.profiler;

import com.google.gson.stream.JsonWriter;
import lombok.val;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfilingNode {
    public final ProfilingNode parent;
    public final String name;
    public final Map<String, ProfilingNode> childrenMap = new HashMap<>();
    private final List<ProfilingNode> children = new ArrayList<>();
    public long totalTime = 0L;
    private long start;
    private String fullNameString = null;

    private ProfilingNode(ProfilingNode parent, String name) {
        this.parent = parent;
        this.name = name.intern();
    }

    public static ProfilingNode createRoot() {
        return new ProfilingNode(null, "");
    }

    public void start() {
        start = System.nanoTime();
    }

    public long end() {
        long delta = System.nanoTime() - start;
        totalTime += delta;
        return delta;
    }

    public ProfilingNode getOrCreateChild(String name) {
        return childrenMap.computeIfAbsent(name, s -> {
            val n = new ProfilingNode(this, s);
            children.add(n);
            return n;
        });
    }

    public String fullName() {
        if (fullNameString == null) {
            fullNameString = (parent.parent != null ? parent.fullName() + "." + name : name).intern();
        }
        return fullNameString;
    }

    public ProfilingNode findChild(String[] path, int index) {
        if (path == null || path.length == 0) {
            return null;
        }
        if (index == path.length) {
            return this;
        }
        val node = path[index];
        if (!childrenMap.containsKey(node)) {
            return null;
        }
        return childrenMap.get(node).findChild(path, index + 1);
    }

    public void toJson(JsonWriter json) throws IOException {
        if (parent != null) {
            json.beginObject();
            json.name("name").value(name);
            json.name("timeNs").value(totalTime);
            json.name("children");
            json.beginArray();
        }
        for (val child : childrenMap.values()) {
            child.toJson(json);
        }
        if (parent != null) {
            json.endArray();
            json.endObject();
        }
    }
}
