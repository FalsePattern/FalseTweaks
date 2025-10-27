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
const cpu_util = @import("src/main/zig/cpuid/cpu_util.zig");
const zanama = @import("zanama");

const base_targets = [_]std.Target.Query{
    .{ .os_tag = .linux, .cpu_arch = .x86_64, .abi = .gnu, .cpu_model = .{ .explicit = &std.Target.x86.cpu.x86_64 } },
    .{ .os_tag = .windows, .cpu_arch = .x86_64, .abi = .gnu, .cpu_model = .{ .explicit = &std.Target.x86.cpu.x86_64 } },
    .{ .os_tag = .linux, .cpu_arch = .aarch64, .abi = .gnu, .cpu_model = .{ .explicit = &std.Target.aarch64.cpu.generic } },
    .{ .os_tag = .macos, .cpu_arch = .aarch64, .abi = .none, .cpu_model = .{ .explicit = &std.Target.aarch64.cpu.generic } },
    .{ .os_tag = .windows, .cpu_arch = .aarch64, .abi = .gnu, .cpu_model = .{ .explicit = &std.Target.aarch64.cpu.generic } },
};

pub fn build(b: *std.Build) void {
    const targets = getTargets(b);

    //JNI stubs
    for (targets.baseline) |target| {
        const triple = target.query.zigTriple(b.allocator) catch @panic("OOM");
        const jni_dep = b.dependency("jni", .{
            .target = target,
            .optimize = .ReleaseFast,
        });
        const jni_module = jni_dep.module("JNI");

        const libjni_mod = b.createModule(.{
            .root_source_file = b.path("src/main/zig/libjni.zig"),
            .target = target,
            .optimize = .ReleaseFast,
            .strip = true,
        });
        const name = std.mem.concat(b.allocator, u8, &.{ "jni-", triple, "-", target.query.cpu_model.explicit.name }) catch @panic("OOM");
        libjni_mod.addImport("jni", jni_module);
        const libjni = b.addLibrary(.{
            .linkage = .dynamic,
            .name = name,
            .root_module = libjni_mod,
        });
        const inst = b.addInstallArtifact(libjni, .{
            .dest_dir = .{ .override = .lib },
            .h_dir = .disabled,
            .implib_dir = .disabled,
            .pdb_dir = .disabled,
        });
        b.getInstallStep().dependOn(&inst.step);
    }

    const zanamaBuilder = ZanamaLibBuilder.init(b);

    zanamaBuilder.addZanamaLibs(
        "cpuid",
        b.createModule(.{
            .root_source_file = b.path("src/main/zig/cpuid/lib.zig"),
            .optimize = .ReleaseSmall,
            .strip = true,
        }),
        targets.baseline,
    );

    zanamaBuilder.addZanamaLibs(
        "FalseTweaks",
        b.createModule(.{
            .root_source_file = b.path("src/main/zig/lib.zig"),
            .optimize = .ReleaseFast,
            .strip = true,
        }),
        targets.all,
    );
}

fn getTargets(b: *std.Build) struct { baseline: []std.Build.ResolvedTarget, all: []std.Build.ResolvedTarget } {
    var baseline_targets: [base_targets.len]std.Build.ResolvedTarget = undefined;
    var targets = std.ArrayList(std.Build.ResolvedTarget).empty;
    defer targets.deinit(b.allocator);
    for (base_targets, 0..) |base, combo_index| {
        baseline_targets[combo_index] = b.resolveTargetQuery(base);
        const supported_models = switch (base.cpu_arch.?) {
            .aarch64 => cpu_util.supported_models_aarch64,
            .x86_64 => cpu_util.supported_models_x86,
            else => @panic(std.mem.concat(b.allocator, u8, &.{ "Unsupported CPU arch ", @tagName(base.cpu_arch.?) }) catch @panic("OOM")),
        };

        for (supported_models) |model| {
            var model_query = base;
            model_query.cpu_model = .{ .explicit = model };
            targets.append(b.allocator, b.resolveTargetQuery(model_query)) catch @panic("OOM");
        }
    }
    return .{
        .baseline = b.allocator.dupe(std.Build.ResolvedTarget, &baseline_targets) catch @panic("OOM"),
        .all = targets.toOwnedSlice(b.allocator) catch @panic("OOM"),
    };
}

const ZanamaLibBuilder = struct {
    b: *std.Build,
    zanama_dep: *std.Build.Dependency,
    zanama_api: *std.Build.Module,
    zb: zanama.Build,

    pub fn init(b: *std.Build) ZanamaLibBuilder {
        const zanama_dep = b.dependency("zanama", .{});
        const zanama_api = zanama_dep.module("api");
        const zb = zanama.Build.init(b, .ReleaseFast, zanama_dep);
        return .{
            .b = b,
            .zanama_dep = zanama_dep,
            .zanama_api = zanama_api,
            .zb = zb,
        };
    }

    fn addZanamaLibs(self: *const ZanamaLibBuilder, name: []const u8, module: *std.Build.Module, targets: []std.Build.ResolvedTarget) void {
        module.addImport("zanama", self.zanama_api);
        const libs = self.zb.createZanamaLibsResolved(name, module, targets);
        const install_step = self.b.getInstallStep();
        for (libs.artifacts) |artifact| {
            const inst = self.b.addInstallArtifact(artifact, .{
                .dest_dir = .{ .override = .lib },
                .h_dir = .disabled,
                .implib_dir = .disabled,
                .pdb_dir = .disabled,
            });
            install_step.dependOn(&inst.step);
        }
        const install_json = self.b.addInstallFile(libs.json, std.mem.concat(self.b.allocator, u8, &.{ name, ".json" }) catch @panic("OOM"));
        install_json.step.dependOn(libs.json_step);
        install_step.dependOn(&install_json.step);
    }
};
