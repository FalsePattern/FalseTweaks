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
const x86 = @import("x86.zig");

pub const cpu = std.Target.x86.cpu;
pub const supported_models = getSupportedModels();
pub const max_name_length = maxNameLength();

const @"64bit" = @intFromEnum(std.Target.x86.Feature.@"64bit");

fn maxNameLength() usize {
    comptime {
        var longest: usize = 0;
        for (supported_models) |model| {
            longest = @max(longest, model.name.len);
        }
        return longest + 1;
    }
}

fn getSupportedModels() [supportedModelCount()]*const CpuModel {
    comptime {
        const decls = @typeInfo(cpu).@"struct".decls;
        var result: [supportedModelCount()]*const CpuModel = undefined;
        var i = 0;
        for (decls) |decl| {
            const cpuModel: *const CpuModel = &@field(cpu, decl.name);
            if (cpuModel == &cpu.generic) {
                continue;
            }
            var features = cpuModel.features;
            features.populateDependencies(&x86.all_features);
            if (features.isEnabled(@"64bit")) {
                result[i] = cpuModel;
                i += 1;
            }
        }
        return result;
    }
}

fn supportedModelCount() usize {
    const decls = @typeInfo(cpu).@"struct".decls;
    var i = 0;
    for (decls) |decl| {
        const cpuModel: *const CpuModel = &@field(cpu, decl.name);
        if (cpuModel == &cpu.generic) {
            continue;
        }
        var features = cpuModel.features;
        features.populateDependencies(&x86.all_features);
        if (features.isEnabled(@"64bit")) {
            i += 1;
        }
    }
    return i;
}
