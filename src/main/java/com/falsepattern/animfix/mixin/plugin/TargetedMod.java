package com.falsepattern.animfix.mixin.plugin;

import com.google.common.io.Files;

import java.nio.file.Path;
import java.util.function.Predicate;

public enum TargetedMod {
    ;

    public final String modName;
    public final Predicate<String> condition;
    public final boolean loadInDevelopment;

    TargetedMod(String modName, boolean loadInDevelopment, Predicate<String> condition) {
        this.modName = modName;
        this.condition = condition;
        this.loadInDevelopment = loadInDevelopment;
    }

    private static Predicate<String> startsWith(String subString) {
        return (name) -> name.startsWith(subString);
    }

    private static Predicate<String> contains(String subString) {
        return (name) -> name.contains(subString);
    }

    private static Predicate<String> matches(String regex) {
        return (name) -> name.matches(regex);
    }

    @SuppressWarnings("UnstableApiUsage")
    public boolean isMatchingJar(Path path) {
        String pathString = path.toString();
        String nameLowerCase = Files.getNameWithoutExtension(pathString).toLowerCase();
        String fileExtension = Files.getFileExtension(pathString);

        return "jar".equals(fileExtension) && condition.test(nameLowerCase);
    }

    @Override
    public String toString() {
        return "TargetedMod{" +
               "modName='" + modName + '\'' +
               '}';
    }
}
