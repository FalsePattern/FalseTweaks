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
        if (!Files.exists(targetPath))
            return;
        try {
            val fileText = Files.readAllLines(targetPath);
            val result = fileText.stream().map(lineTransformer).collect(Collectors.toList());
            Files.write(targetPath, result);
        } catch (IOException e) {
            exceptionHandler.accept(e);
        }
    }
}
