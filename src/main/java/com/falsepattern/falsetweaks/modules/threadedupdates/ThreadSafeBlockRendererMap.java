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

import com.falsepattern.falsetweaks.Share;
import com.falsepattern.falsetweaks.api.threading.ThreadSafeBlockRenderer;
import lombok.SneakyThrows;
import lombok.val;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

import java.util.HashMap;
import java.util.Map;

import static com.falsepattern.falsetweaks.config.ThreadingConfig.SUPPRESS_HANDLER_REGISTRATION_LOGGING;

public class ThreadSafeBlockRendererMap extends HashMap<Integer, ISimpleBlockRenderingHandler> {
    public ThreadSafeBlockRendererMap(Map<? extends Integer, ? extends ISimpleBlockRenderingHandler> m) {
        super(m);
    }

    @Override
    public ISimpleBlockRenderingHandler put(Integer key, ISimpleBlockRenderingHandler value) {
        if (!SUPPRESS_HANDLER_REGISTRATION_LOGGING) {
            if (!(value instanceof ThreadSafeBlockRenderer) && value != null) {
                Share.log.warn("Registered new block rendering handler which is not thread safe at id " + key + ":" + value.getClass().getName(), new Throwable());
            } else if (value != null) {
                Share.log.info("Registered thread safe block rendering handler at id " + key + ":" + value.getClass().getName());
            } else {
                Share.log.warn("Registered null block rendering handler at id " + key, new Throwable());
            }
        }
        return super.put(key, value);
    }

    @Override
    public ISimpleBlockRenderingHandler get(Object key) {
        val obj = super.get(key);
        if (obj instanceof ThreadSafeBlockRenderer) {
            return ((ThreadSafeBlockRenderer) obj).forCurrentThread();
        }
        return obj;
    }

    @SneakyThrows
    public static void inject() {
        val blockRenderers = RenderingRegistry.class.getDeclaredField("blockRenderers");
        blockRenderers.setAccessible(true);
        @SuppressWarnings("deprecation")
        val rr = RenderingRegistry.instance();
        @SuppressWarnings({"unchecked"})
        val oldRenderers = (Map<Integer, ISimpleBlockRenderingHandler>) blockRenderers.get(rr);
        val newRenderers = new ThreadSafeBlockRendererMap(oldRenderers);
        blockRenderers.set(rr, newRenderers);
    }
}
