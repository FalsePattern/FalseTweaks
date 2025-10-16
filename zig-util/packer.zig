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

    const cwd = std.fs.cwd();

    const output_file_path = args.next() orelse @panic("No output file");
    const output_file = try cwd.createFile(output_file_path, .{});
    defer output_file.close();
    var output_buf: [4096]u8 = undefined;
    var output_writer = output_file.writer(&output_buf);
    defer output_writer.interface.flush() catch @panic("Flush error");
    const writer = &output_writer.interface;
    var count: u32 = 0;
    try writer.writeInt(u32, 0, .big);

    while (true) {
        const name = args.next() orelse break;
        const file_path = args.next() orelse break;
        count += 1;

        try writer.writeInt(u8, @intCast(name.len), .big);
        try writer.writeAll(name);

        try writer.flush();

        const size_pos = output_writer.pos;

        try writer.writeInt(u32, 0, .big);

        var size: u32 = 0;

        {
            const input_file = try cwd.openFile(file_path, .{});
            defer input_file.close();
            var input_buf: [4096]u8 = undefined;
            var input_reader = input_file.reader(&input_buf);
            size = @intCast(try writer.sendFileAll(&input_reader, .unlimited));
        }
        try writer.flush();
        try output_writer.seekTo(size_pos);
        try writer.writeInt(u32, size, .big);
        try writer.flush();
        try output_writer.seekTo(try output_file.getEndPos());
    }

    try writer.flush();
    try output_writer.seekTo(0);
    try writer.writeInt(u32, count, .big);
}