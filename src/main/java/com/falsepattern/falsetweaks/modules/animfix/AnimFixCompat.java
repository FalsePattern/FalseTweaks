package com.falsepattern.falsetweaks.modules.animfix;

import com.falsepattern.falsetweaks.Share;
import com.falsepattern.lib.util.FileUtil;
import lombok.val;

import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;

public class AnimFixCompat {
    public static void executeConfigCompatibilityHacks() {
        HodgePodgeCompat.executeHodgePodgeCompatibilityHacks();
    }

    public static class HodgePodgeCompat {
        public static void executeHodgePodgeCompatibilityHacks() {
            val targetPath = FileUtil.getMinecraftHomePath().resolve("config").resolve("hodgepodge.cfg");
            if (!Files.exists(targetPath))
                return;
            try {
                val fileText = Files.readAllLines(targetPath);
                val result = fileText.stream().map(line -> {
                    if (line.contains("optimizeTextureLoading")) {
                        return line.replace("true", "false");
                    }
                    return line;
                }).collect(Collectors.toList());
                Files.write(targetPath, result);
            } catch (IOException e) {
                Share.log.fatal("Failed to apply HodgePodge texture optimization compatibility patches!", e);
            }
        }
    }
}
