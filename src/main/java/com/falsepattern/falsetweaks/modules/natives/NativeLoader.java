/*
 * This file is part of MEGATrace.
 *
 * Copyright (C) 2024-2025 The MEGA Team
 * All Rights Reserved
 *
 * The above copyright notice, this permission notice and the word "MEGA"
 * shall be included in all copies or substantial portions of the Software.
 *
 * MEGATrace is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * MEGATrace is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with MEGATrace.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.falsetweaks.modules.natives;

import com.falsepattern.falsetweaks.Share;
import com.falsepattern.falsetweaks.Tags;
import com.falsepattern.lib.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.var;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class NativeLoader {
    private final Path nativesDir;
    private final Triple currentTriple;
    private final Unpacker unpacker;

    public NativeLoader(Class<?> root) throws UnsupportedPlatformException {
        var homeDirStr = System.getProperty("minecraft.sharedDataDir");
        if (homeDirStr == null) {
            homeDirStr = System.getenv("MINECRAFT_SHARED_DATA_DIR");
        }
        final Path homeDir;
        if (homeDirStr == null) {
            homeDir = FileUtil.getMinecraftHomePath();
        } else {
            homeDir = Paths.get(homeDirStr);
        }
        nativesDir = homeDir.resolve("falsepattern").resolve("natives").resolve(Tags.MOD_ID);
        try {
            Files.createDirectories(nativesDir);
        } catch (IOException ignored) {
        }
        val arch = Arch.getCurrent();
        val os = OS.getCurrent();
        val libc = os.libc();
        currentTriple = new Triple(arch, os, libc);
        final String pak;
        switch(OS.getCurrent()) {
            case Windows: pak = "windows.pak"; break;
            case Linux: pak = "linux.pak"; break;
            case MacOS: pak = "macos.pak"; break;
            default: throw new IllegalStateException();
        }
        val res = root.getResource(pak);
        if (res == null) {
            throw new UnsupportedPlatformException("No resource package for OS " + os.getName());
        }
        unpacker = new Unpacker(res);
        try {
            Share.log.info("Pack file contents: {}", Arrays.toString(unpacker.names()));
        } catch (IOException ignored) {

        }
    }

    public String loadNative(String libName) throws UnsupportedPlatformException {
        val libNameSys = currentTriple.toLibName(libName);
        val libFile = nativesDir.resolve(libNameSys);
        try {
            unpacker.unpack(libName, libFile);
        } catch (IOException e) {
            throw new UnsupportedPlatformException(e);
        }
        val absolutePath = libFile.toAbsolutePath().toString();
        try {
            System.load(absolutePath);
        } catch (UnsatisfiedLinkError e) {
            throw new UnsupportedPlatformException(e);
        }
        return absolutePath;
    }

    @RequiredArgsConstructor
    private static class Triple {
        private final Arch arch;
        private final OS os;
        private final LibC libc;

        public String toLibName(String libName) {
            val nameBuilder = new StringBuilder();
            switch (os) {
                case Linux:
                case MacOS:
                    nameBuilder.append("lib");
            }
            nameBuilder.append(libName);
            nameBuilder.append('-').append(arch.getName()).append('-').append(os.getName()).append('-').append(libc.getName());
            switch (os) {
                case Windows: {
                    nameBuilder.append(".dll");
                    break;
                }
                case Linux: {
                    nameBuilder.append(".so");
                    break;
                }
                case MacOS: {
                    nameBuilder.append(".dylib");
                    break;
                }
            }
            return nameBuilder.toString();
        }
    }

    private enum OS {
        Windows,
        Linux,
        MacOS;

        public static OS getCurrent() throws UnsupportedPlatformException {
            String osName = System.getProperty("os.name");
            if (osName.startsWith("Windows")) {
                return Windows;
            } else if (osName.startsWith("Linux") || osName.startsWith("FreeBSD") || osName.startsWith("SunOS") || osName.startsWith("Unix")) {
                return Linux;
            } else if (osName.startsWith("Mac OS X") || osName.startsWith("Darwin")) {
                return MacOS;
            } else {
                throw new UnsupportedPlatformException("Unsupported OS " + osName);
            }
        }

        public String getName() {
            switch (this) {
                case Windows:
                    return "windows";
                case Linux:
                    return "linux";
                case MacOS:
                    return "macos";
                default:
                    throw new AssertionError("Unreachable");
            }
        }

        public LibC libc() {
            switch (this) {
                case MacOS:
                    return LibC.None;
                default:
                    return LibC.GNU; // TODO musl
            }
        }
    }

    @RequiredArgsConstructor
    private enum Arch {
        X64(true),
        X86(false),
        ARM64(true),
        ARM32(false);

        final boolean is64Bit;

        static Arch getCurrent() throws UnsupportedPlatformException {
            String osArch = System.getProperty("os.arch");
            boolean is64Bit = osArch.contains("64") || osArch.startsWith("armv8");

            boolean isArm = osArch.startsWith("arm") || osArch.startsWith("aarch64");
            boolean isX86 = osArch.startsWith("x86") || osArch.startsWith("amd64");
            if (isArm) {
                return is64Bit ? ARM64 : ARM32;
            }
            if (isX86) {
                return is64Bit ? X64 : X86;
            }
            throw new UnsupportedPlatformException("Unsupported CPU architecture " + osArch);
        }

        public String getName() {
            switch (this) {
                case X86:
                    return "x86";
                case X64:
                    return "x86_64";
                case ARM32:
                    return "arm";
                case ARM64:
                    return "aarch64";
                default:
                    throw new AssertionError("Unreachable");
            }
        }
    }

    private enum LibC {
        GNU,
        //        MUSL, TODO
        None;

        public String getName() {
            switch (this) {
                case GNU:
                    return "gnu";
                //                case MUSL: return "musl"; TODO
                case None:
                    return "none";
                default:
                    throw new AssertionError("Unreachable");
            }
        }
    }
}
