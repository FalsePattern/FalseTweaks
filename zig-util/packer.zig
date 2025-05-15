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

pub fn main() !void {
    var allocator = std.heap.DebugAllocator(.{}).init;
    const alloc = allocator.allocator();
    var args = try std.process.argsWithAllocator(alloc);
    _ = args.next() orelse @panic("???");

    const output_file_path = args.next() orelse @panic("No output file");
    const output_file = try std.fs.createFileAbsolute(output_file_path, .{});
    defer output_file.close();
    var buffered_writer = std.io.bufferedWriter(output_file.writer());
    defer buffered_writer.flush() catch @panic("Flush error");
    const writer = buffered_writer.writer();

    var count: u32 = 0;
    try writer.writeInt(u32, 0, .big);

    while (true) {
        const name = args.next() orelse break;
        const file_path = args.next() orelse break;
        count += 1;

        try writer.writeInt(u8, @intCast(name.len), .big);
        try writer.writeAll(name);

        try buffered_writer.flush();

        const size_pos = try output_file.getPos();

        try writer.writeInt(u32, 0, .big);

        var size: u32 = 0;

        {
            const input_file = try std.fs.openFileAbsolute(file_path, .{});
            defer input_file.close();
            var buf: [4096]u8 = undefined;
            while (true) {
                const read_size = try input_file.read(&buf);
                if (read_size == 0)
                    break;
                size += @intCast(read_size);
                try writer.writeAll(buf[0..read_size]);
            }
        }
        try buffered_writer.flush();
        try output_file.seekTo(size_pos);
        try writer.writeInt(u32, size, .big);
        try buffered_writer.flush();
        try output_file.seekFromEnd(0);
    }

    try buffered_writer.flush();
    try output_file.seekTo(0);
    try writer.writeInt(u32, count, .big);
}