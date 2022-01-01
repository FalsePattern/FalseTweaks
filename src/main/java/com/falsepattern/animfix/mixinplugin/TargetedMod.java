package com.falsepattern.animfix.mixinplugin;

import com.google.common.io.Files;
import lombok.val;

import java.nio.file.Path;

public enum TargetedMod {

    VANILLA("Minecraft", "unused", true);

    public final String modName;
    public final String jarNamePrefixLowercase;

    public final boolean loadInDevelopment;

    TargetedMod(String modName, String jarNamePrefix, boolean loadInDevelopment) {
        this.modName = modName;
        this.jarNamePrefixLowercase = jarNamePrefix.toLowerCase();
        this.loadInDevelopment = loadInDevelopment;
    }

    @SuppressWarnings("UnstableApiUsage")
    public boolean isMatchingJar(Path path) {
        val pathString = path.toString();
        val nameLowerCase = Files.getNameWithoutExtension(pathString).toLowerCase();
        val fileExtension = Files.getFileExtension(pathString);

        return nameLowerCase.startsWith(jarNamePrefixLowercase) && "jar".equals(fileExtension);
    }

    @Override
    public String toString() {
        return "TargetedMod{" +
                "modName='" + modName + '\'' +
                ", jarNamePrefixLowercase='" + jarNamePrefixLowercase + '\'' +
                '}';
    }
}
