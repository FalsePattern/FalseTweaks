package com.falsepattern.falsetweaks.mixin.helper;

public class RegexHelper {
    public static boolean zipJarRegex(String toMatch) {
        return toMatch.endsWith(".jar") || toMatch.endsWith(".zip");
    }

    public static boolean classFileRegex(String toMatch) {
        return toMatch.endsWith(".class") && !toMatch.startsWith("$") && !toMatch.endsWith("$.class");
    }

    public static boolean modClassRegex(String toMatch) {
        String shortName = toMatch.substring(toMatch.lastIndexOf('.') + 1);
        return shortName.startsWith("mod_") && !shortName.contains("$");
    }
}
