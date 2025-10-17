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

const builtin = @import("builtin");
const std = @import("std");
const jni = @import("jni");
const jni_util = @import("jni_util.zig");

pub const Impl = struct {
    var fX_: Vec6 = undefined;
    var fY_: Vec6 = undefined;
    var fZ_: Vec6 = undefined;
    var fW_: Vec6 = undefined;

    pub fn setFrustum(frust: [*]const f32) callconv(.c) void {
        fX_ = frust[0..6].*;
        fY_ = frust[6..12].*;
        fZ_ = frust[12..18].*;
        fW_ = frust[18..24].*;
    }

    inline fn fma(T: type, a: T, b: T, c: T) T {
        @setFloatMode(.optimized);
        //@mulAdd on non-fma capable CPUs bundles the compiler runtime
        if (comptime builtin.cpu.features.isEnabled(@as(std.Target.Cpu.Feature.Set.Index, @intFromEnum(std.Target.x86.Feature.fma)))) {
            return @mulAdd(T, a, b, c);
        } else {
            return (a * b) + c;
        }
    }

    const Vec6 = @Vector(6, f32);
    const Vec6bool = @Vector(6, bool);

    pub fn isBoxInFrustum(minX: f32, minY: f32, minZ: f32, maxX: f32, maxY: f32, maxZ: f32) callconv(.c) bool {
        @setFloatMode(.optimized);
        const fX = fX_;
        const fY = fY_;
        const fZ = fZ_;
        const fW = fW_;

        const FmX = fX * @as(Vec6, @splat(minX));
        const FMX = fX * @as(Vec6, @splat(maxX));
        const FmY = fY * @as(Vec6, @splat(minY));
        const FMY = fY * @as(Vec6, @splat(maxY));

        const F_mZ_W = fma(Vec6, fZ, @splat(minZ), fW);
        const F_MZ_W = fma(Vec6, fZ, @splat(maxZ), fW);

        const F_mY_mZ_W = FmY + F_mZ_W;
        const F_MY_mZ_W = FMY + F_mZ_W;

        const F_mY_MZ_W = FmY + F_MZ_W;
        const F_MY_MZ_W = FMY + F_MZ_W;

        const zr: Vec6 = @splat(0);
        var eq: Vec6bool = undefined;
        eq = @bitCast(@intFromBool(FmX + F_mY_mZ_W < zr) &
            @intFromBool(FMX + F_mY_mZ_W < zr) &
            @intFromBool(FmX + F_MY_mZ_W < zr) &
            @intFromBool(FMX + F_MY_mZ_W < zr) &
            @intFromBool(FmX + F_mY_MZ_W < zr) &
            @intFromBool(FMX + F_mY_MZ_W < zr) &
            @intFromBool(FmX + F_MY_MZ_W < zr) &
            @intFromBool(FMX + F_MY_MZ_W < zr));

        return !@reduce(.Or, eq);
    }
};

pub const JNI = struct {
    var setFrustum: *const fn (frust: [*]const f32) callconv(.c) void = undefined;
    var isBoxInFrustum: *const fn (minX: f32, minY: f32, minZ: f32, maxX: f32, maxY: f32, maxZ: f32) callconv(.c) bool = undefined;

    var lib: std.DynLib = undefined;

    pub fn @"Java.link"(c_env: *jni.cEnv, _: jni.jclass, lib_path_jstr: jni.jstring) callconv(.c) jni.jboolean {
        const lib_path_str = lib_path_jstr orelse return 0;
        const env = jni.JNIEnv.warp(c_env);
        var isCopy: bool = undefined;
        const lib_path = env.getStringUTFChars(lib_path_str, &isCopy);
        defer env.releaseStringUTFChars(lib_path_jstr, lib_path);
        const len = std.mem.len(lib_path);
        lib = std.DynLib.open(lib_path[0..len]) catch return 0;
        setFrustum = lib.lookup(@TypeOf(setFrustum), "falsetweaks.Clipping::setFrustum") orelse return 0;
        isBoxInFrustum = lib.lookup(@TypeOf(isBoxInFrustum), "falsetweaks.Clipping::isBoxInFrustum") orelse return 0;
        return 1;
    }

    pub fn @"Java.setFrustum"(c_env: *jni.cEnv, _: jni.jclass, frustum_java_arr: jni.jfloatArray) callconv(.c) void {
        if (frustum_java_arr == null) {
            return;
        }
        const env = jni.JNIEnv.warp(c_env);
        var is_copy: bool = undefined;
        const frustum_arr = env.getPrimitiveArrayElements(f32, frustum_java_arr, &is_copy);
        defer env.releasePrimitiveArrayElements(f32, frustum_java_arr, frustum_arr, .JNIAbort);
        setFrustum(frustum_arr);
    }

    pub fn @"JavaCritical.setFrustum"(_: jni.jint, frustum_java_arr: [*c]jni.jfloat) callconv(.c) void {
        setFrustum(frustum_java_arr);
    }

    pub fn @"Java.isBoxInFrustum"(_: *jni.cEnv, _: jni.jclass, minX: jni.jfloat, minY: jni.jfloat, minZ: jni.jfloat, maxX: jni.jfloat, maxY: jni.jfloat, maxZ: jni.jfloat) callconv(.c) jni.jboolean {
        return jni.boolToJboolean(isBoxInFrustum(minX, minY, minZ, maxX, maxY, maxZ));
    }

    pub fn @"JavaCritical.isBoxInFrustum"(minX: jni.jfloat, minY: jni.jfloat, minZ: jni.jfloat, maxX: jni.jfloat, maxY: jni.jfloat, maxZ: jni.jfloat) callconv(.c) jni.jboolean {
        return jni.boolToJboolean(isBoxInFrustum(minX, minY, minZ, maxX, maxY, maxZ));
    }
};
