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
const x86 = @import("src/main/zig/cpuid/x86_util.zig");
const zanama = @import("zanama");
pub fn build(b: *std.Build) void {
    const zanama_dep = b.dependency("zanama", .{});
    const zb = zanama.Build.init(b, .ReleaseFast, zanama_dep);

    const packer = b.addExecutable(.{
        .name = "packer",
        .root_module = b.addModule("packer", .{
            .root_source_file = b.path("zig-util/packer.zig"),
            .target = b.graph.host,
        }),
    });
    const run_packer = b.addRunArtifact(packer);
    const natives_file = "natives.pak";
    const install_step = b.getInstallStep();
    install_step.dependOn(&b.addInstallLibFile(run_packer.addOutputFileArg(natives_file), natives_file).step);
    for ([_]std.Target.Os.Tag{.windows, .linux}) |os| {
        const baseline_target = b.resolveTargetQuery(.{
            .os_tag = os,
            .cpu_arch = .x86_64,
            .cpu_model = .{ .explicit = &std.Target.x86.cpu.x86_64 },
            .abi = .gnu,
        });
        {
            const jni_dep = b.dependency("jni", .{
                .target = baseline_target,
                .optimize = .ReleaseFast,
            });
            const jni_module = jni_dep.module("JNI");

            const libjni_mod = b.createModule(.{
                .root_source_file = b.path("src/main/zig/libjni.zig"),
                .target = baseline_target,
                .optimize = .ReleaseFast,
                .strip = true,
            });
            const name = std.mem.concat(b.allocator, u8, &.{"jni-", @tagName(os)}) catch @panic("OOM");
            libjni_mod.addImport("jni", jni_module);
            const libjni = b.addLibrary(.{
                .linkage = .dynamic,
                .name = name,
                .root_module = libjni_mod,
            });
            run_packer.addArg(name);
            run_packer.addFileArg(libjni.getEmittedBin());
        }
        {
            const libcpuid_mod = b.createModule(.{
                .root_source_file = b.path("src/main/zig/cpuid/lib.zig"),
                .target = baseline_target,
                .optimize = .ReleaseSmall,
                .strip = true,
                .imports = &.{
                    .{ .name = "zanama", .module = zanama_dep.module("api") },
                }
            });
            const libs = zb.createZanamaLibsResolved("cpuid", libcpuid_mod, &.{baseline_target});
            for (libs.artifacts) |artifact| {
                run_packer.addArg(artifact.name);
                run_packer.addFileArg(artifact.getEmittedBin());
            }
            const install_json = b.addInstallFile(libs.json, "cpuid.json");
            install_json.step.dependOn(libs.json_step);
            install_step.dependOn(&install_json.step);
        }
        {
            var targets: [x86.supported_models.len]std.Build.ResolvedTarget = undefined;
            for (x86.supported_models, 0..) |model, i| {
                targets[i] = b.resolveTargetQuery(.{
                    .os_tag = os,
                    .cpu_arch = .x86_64,
                    .cpu_model = .{ .explicit = model },
                    .abi = .gnu,
                });
            }
            const lib_mod = b.createModule(.{
                .root_source_file = b.path("src/main/zig/lib.zig"),
                .optimize = .ReleaseFast,
                .strip = true,
                .imports = &.{
                    .{ .name = "zanama", .module = zanama_dep.module("api") },
                }
            });

            const libs = zb.createZanamaLibsResolved("FalseTweaks", lib_mod, &targets);

            for (libs.artifacts) |artifact| {
                run_packer.addArg(artifact.name);
                run_packer.addFileArg(artifact.getEmittedBin());
            }
            const install_json = b.addInstallFile(libs.json, "FalseTweaks.json");
            install_json.step.dependOn(libs.json_step);
            install_step.dependOn(&install_json.step);
        }
    }
}
