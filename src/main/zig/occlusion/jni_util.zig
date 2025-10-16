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

const jni = @import("jni");

pub inline fn getByteArray(env: jni.JNIEnv, array: jni.jbyteArray) ?[:0]u8 {
    if (array == null)
        return null;
    var isCopy: bool = undefined;
    const length: u32 = @bitCast(env.getArrayLength(array));
    const arr = env.getPrimitiveArrayElements(jni.jbyte, array, &isCopy)[0..length];
    return @ptrCast(arr);
}

pub inline fn freeByteArray(env: jni.JNIEnv, array: jni.jbyteArray, arr: [:0]u8) void {
    env.releasePrimitiveArrayElements(jni.jbyte, array, @ptrCast(arr.ptr), .JNIDefault);
}

pub inline fn critical_getByteArray(len: jni.jint, ptr: [*c]jni.jbyte) ?[:0]const u8 {
    if (ptr) |s| {
        const l: u32 = @bitCast(len);
        return @ptrCast(s[0..l]);
    } else {
        return null;
    }
}
