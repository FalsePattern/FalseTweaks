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
package com.falsepattern.falsetweaks.modules.threadedupdates;

import com.falsepattern.falsetweaks.api.threading.ThreadSafeBlockRenderer;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Loader;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.falsepattern.falsetweaks.config.ThreadingConfig.LOG_ISBRH_ERRORS;

public class ThreadSafeBlockRendererMap implements Map<Integer, ISimpleBlockRenderingHandler> {
    @Data
    private static class ISBRHErrorMessage {
        public final int id;
        public final String className;
        public final Kind kind;
        public final Throwable stacktrace;

        public enum Kind {
            Null,
            NonThreadSafe
        }
    }

    private static final Map<String, List<ISBRHErrorMessage>> errorMessages = new HashMap<>();
    private static boolean bypass = false;
    private final Map<Integer, ISimpleBlockRenderingHandler> wrappedMap = new HashMap<>();
    @Override
    public ISimpleBlockRenderingHandler put(Integer key, ISimpleBlockRenderingHandler value) {
        log:
        if (LOG_ISBRH_ERRORS) {
            synchronized (errorMessages) {
                if (value instanceof ThreadSafeBlockRenderer) {
                    break log;
                }
                final String activeModID;
                if (bypass) {
                    activeModID = "UNKNOWN";
                } else {
                    val activeModContainer = Loader.instance().activeModContainer();
                    activeModID = activeModContainer == null ? "UNKNOWN" : activeModContainer.getModId();
                }
                errorMessages.computeIfAbsent(activeModID, i -> new ArrayList<>())
                             .add(new ISBRHErrorMessage(key, value == null ? "null" : value.getClass().getName(),
                                                        value == null ? ISBRHErrorMessage.Kind.Null : ISBRHErrorMessage.Kind.NonThreadSafe, new Throwable()));
            }
        }
        return wrappedMap.put(key, value);
    }

    @Override
    public ISimpleBlockRenderingHandler remove(Object key) {
        return threadSafeWrap(wrappedMap.remove(key));
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends ISimpleBlockRenderingHandler> m) {
        for (val entry: m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        wrappedMap.clear();
    }

    @Override
    public Set<Integer> keySet() {
        return wrappedMap.keySet();
    }

    @Override
    public Collection<ISimpleBlockRenderingHandler> values() {
        return wrappedMap.values().stream().map(ThreadSafeBlockRendererMap::threadSafeWrap).collect(Collectors.toList());
    }

    @Override
    public Set<Entry<Integer, ISimpleBlockRenderingHandler>> entrySet() {
        return wrappedMap.entrySet().stream().map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), threadSafeWrap(entry.getValue()))).collect(Collectors.toSet());
    }

    private static ISimpleBlockRenderingHandler threadSafeWrap(ISimpleBlockRenderingHandler isbrh) {
        if (isbrh instanceof ThreadSafeBlockRenderer)
            return ((ThreadSafeBlockRenderer) isbrh).forCurrentThread();
        return isbrh;
    }

    @Override
    public int size() {
        return wrappedMap.size();
    }

    @Override
    public boolean isEmpty() {
        return wrappedMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return wrappedMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return wrappedMap.containsValue(value);
    }

    @Override
    public ISimpleBlockRenderingHandler get(Object key) {
        return threadSafeWrap(wrappedMap.get(key));
    }



    @SneakyThrows
    public static void inject() {
        val blockRenderers = RenderingRegistry.class.getDeclaredField("blockRenderers");
        blockRenderers.setAccessible(true);
        @SuppressWarnings("deprecation") val rr = RenderingRegistry.instance();
        @SuppressWarnings({"unchecked"}) val oldRenderers = (Map<Integer, ISimpleBlockRenderingHandler>) blockRenderers.get(rr);
        val newRenderers = new ThreadSafeBlockRendererMap();
        bypass = true;
        newRenderers.putAll(oldRenderers);
        bypass = false;
        blockRenderers.set(rr, newRenderers);
    }


    public static void logBrokenISBRHs() {
        if (errorMessages.isEmpty() || !LOG_ISBRH_ERRORS) {
            return;
        }
        Logger logger = LogManager.getLogger("THREAD SAFETY");
        logger.error(Strings.repeat("+=", 25));
        logger.error("The following ISBRH errors were found.");
        for (val error : errorMessages.entrySet()) {
            val mod = error.getKey();
            val messages = error.getValue();
            logger.error(Strings.repeat("=", 50));
            logger.error("  MOD {} has {} unsafe ISBRH{}", mod, messages.size(), messages.size() != 1 ? "s" : "");
            for (val message : messages) {
                switch (message.kind) {
                    case Null:
                        logger.error("    ID {} is null!", message.id);
                        break;
                    case NonThreadSafe:
                        logger.error("    ID {} is not thread safe! Renderer class: {}", message.id, message.className);
                }
                logger.trace(message.stacktrace);
            }
            logger.error(Strings.repeat("=", 50));
        }
        logger.error("Open the log file to view detailed stacktraces!");
        logger.error(Strings.repeat("+=", 25));
    }
}
