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
package com.falsepattern.falsetweaks.api.threading;

import com.falsepattern.lib.StableAPI;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

/**
 * This gets injected into classes by the THREAD_SAFE_ISBRHS config option, along with a static final threadlocal, with the initial instance generated via the default constructor
 * of said renderer.
 * <b>
 * This class is this tiny to make ASM work and stubbing it easier (reducing coupling between projects).
 */
@StableAPI(since = "3.0.0")
public interface ThreadSafeBlockRenderer {
    @StableAPI.Expose
    ISimpleBlockRenderingHandler forCurrentThread();
}
