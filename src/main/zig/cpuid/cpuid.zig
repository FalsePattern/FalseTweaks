// FalseTweaks
//
// Copyright (C) 2022-2025 FalsePattern
// All Rights Reserved
//
// The above copyright notice and this permission notice shall be included
// in all copies or substantial portions of the Software.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, only version 3 of the License.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.

const std = @import("std");
const builtin = @import("builtin");
const CpuModel = std.Target.Cpu.Model;
const cpu_util = @import("cpu_util.zig");
const jni = @import("jni");

pub const Impl = struct {
    fn _has(cpu: std.Target.Cpu.Feature.Set, feature: std.Target.x86.Feature) bool {
        return cpu.isEnabled(@as(std.Target.Cpu.Feature.Set.Index, @intFromEnum(feature)));
    }

    fn has(cpu: std.Target.Cpu.Feature.Set, features: []const std.Target.x86.Feature) bool {
        for (features) |feature| {
            if (!_has(cpu, feature))
                return false;
        }
        return true;
    }

    fn getModel() *const CpuModel {
        switch (builtin.cpu.arch) {
            .x86_64 => {
                const target = std.zig.system.resolveTargetQuery(.{
                    .os_tag = builtin.os.tag,
                    .cpu_arch = builtin.target.cpu.arch,
                    .cpu_model = .native,
                    .abi = .gnu,
                }) catch return builtin.target.cpu.model;
                const cpu = target.cpu;
                const x86_cpu = std.Target.x86.cpu;
                // if (model == &x86_cpu.x86_64) {
                var version = &x86_cpu.x86_64;
                if (has(cpu.features, &.{ .cx16, .sahf, .popcnt, .sse3, .sse4_1, .sse4_2, .ssse3 })) {
                    version = &x86_cpu.x86_64_v2;
                } else return version;
                if (has(cpu.features, &.{ .avx, .avx2, .bmi, .bmi2, .f16c, .fma, .lzcnt, .movbe, .xsave })) {
                    version = &x86_cpu.x86_64_v3;
                } else return version;
                if (has(cpu.features, &.{ .avx512f, .avx512bw, .avx512cd, .avx512dq, .avx512vl })) {
                    version = &x86_cpu.x86_64_v4;
                } else return version;
                // }
                // for (cpu_util.supported_models_x86) |supp_model| {
                //     if (supp_model == model) {
                //         return model;
                //     }
                // }
                return version;
            },
            .aarch64 => {
                return &std.Target.aarch64.cpu.generic;
            },
            else => @compileError("Unsupported CPU arch " ++ @tagName(builtin.cpu.arch)),
        }
    }

    pub fn maxNameLength() callconv(.c) usize {
        return cpu_util.max_name_length_current;
    }

    pub fn getCpuModel(buf: [*]u8) callconv(.c) [*:0]const u8 {
        const model = getModel();
        @memcpy(buf[0..model.name.len], model.name);
        buf[model.name.len] = 0;
        return @ptrCast(buf);
    }
};

pub const JNI = struct {
    var getCpuModel: *const fn (buf: [*]u8) callconv(.c) [*:0]const u8 = undefined;

    var lib: std.DynLib = undefined;

    pub fn @"Java.link"(c_env: *jni.cEnv, _: jni.jclass, lib_path_jstr: jni.jstring) callconv(.c) jni.jboolean {
        const lib_path_str = lib_path_jstr orelse return 0;
        const env = jni.JNIEnv.warp(c_env);
        var isCopy: bool = undefined;
        const lib_path = env.getStringUTFChars(lib_path_str, &isCopy);
        defer env.releaseStringUTFChars(lib_path_jstr, lib_path);
        const len = std.mem.len(lib_path);
        lib = std.DynLib.open(lib_path[0..len]) catch return 0;
        getCpuModel = lib.lookup(@TypeOf(getCpuModel), "falsetweaks.CpuID::getCpuModel") orelse return 0;
        return 1;
    }

    pub fn @"Java.getCpuModel"(c_env: *jni.cEnv, _: jni.jclass) callconv(.c) jni.jstring {
        const env = jni.JNIEnv.warp(c_env);
        var buf: [cpu_util.max_name_length_current]u8 = undefined;
        return env.newStringUTF(getCpuModel(&buf));
    }
};
