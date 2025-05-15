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
package com.falsepattern.falsetweaks.util;

import com.falsepattern.lib.util.FileUtil;
import lombok.val;

import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConfigFixUtil {
    public static void fixConfig(String configFile, Function<String, String> lineTransformer, Consumer<IOException> exceptionHandler) {
        val targetPath = FileUtil.getMinecraftHomePath().resolve("config").resolve(configFile);
        if (!Files.exists(targetPath)) {
            return;
        }
        try {
            val fileText = Files.readAllLines(targetPath);
            val result = fileText.stream().map(lineTransformer).collect(Collectors.toList());
            Files.write(targetPath, result);
        } catch (IOException e) {
            exceptionHandler.accept(e);
        }
    }
}
