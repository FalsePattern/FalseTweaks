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

pub const supported_models_x86 = getSupportedModelsX86();
pub const max_name_length_x86 = maxNameLength(supported_models_x86);
pub const supported_models_aarch64 = getSupportedModelsAarch64();
pub const max_name_length_aarch64 = maxNameLength(supported_models_aarch64);

pub const max_name_length_current = switch (builtin.cpu.arch) {
    .x86_64 => max_name_length_x86,
    .aarch64 => max_name_length_aarch64,
    else => @compileError("Unsupported CPU arch " ++ @tagName(builtin.cpu.arch)),
};

fn getSupportedModelsX86() []const *const CpuModel {
    comptime {
        const cpu = std.Target.x86.cpu;
        const decls = @typeInfo(cpu).@"struct".decls;
        var result: []const *const CpuModel = &[0]*const CpuModel{};
        var i = 0;
        for (decls) |decl| {
            const cpuModel: *const CpuModel = &@field(cpu, decl.name);
            if (cpuModel == &cpu.generic) {
                continue;
            }
            var features = cpuModel.features;
            features.populateDependencies(&std.Target.x86.all_features);
            if (features.isEnabled(@intFromEnum(std.Target.x86.Feature.@"64bit"))) {
                result = result ++ &[_]*const CpuModel{cpuModel};
                i += 1;
            }
        }
        return result;
    }
}

fn getSupportedModelsAarch64() []const *const CpuModel {
    comptime {
        const cpu = std.Target.aarch64.cpu;
        const decls = @typeInfo(cpu).@"struct".decls;
        var result: []const *const CpuModel = &[0]*const CpuModel{};
        for (decls) |decl| {
            const cpuModel: *const CpuModel = &@field(cpu, decl.name);
            result = result ++ &[_]*const CpuModel{cpuModel};
        }
        return result;
    }
}

fn maxNameLength(models: []const *const CpuModel) usize {
    comptime {
        var longest: usize = 0;
        for (models) |model| {
            longest = @max(longest, model.name.len);
        }
        return longest + 1;
    }
}