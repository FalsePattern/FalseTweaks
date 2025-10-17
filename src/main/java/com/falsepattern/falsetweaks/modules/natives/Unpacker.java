/*
 * This file is part of FalseTweaks.
 *
 * Copyright (C) 2022-2025 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * FalseTweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * FalseTweaks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FalseTweaks. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.falsetweaks.modules.natives;

import lombok.Cleanup;
import lombok.val;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class Unpacker {
    private final URL data;
    public Unpacker(URL data) {
        this.data = data;
    }

    public String[] names() throws IOException {
        @Cleanup val in = data.openStream();
        @Cleanup val dIn = new DataInputStream(in);
        val count = dIn.readInt();
        val result = new String[count];
        for (int i = 0; i < count; i++) {
            val nameLength = dIn.readUnsignedByte();
            val nameBuf = new byte[nameLength];
            dIn.readFully(nameBuf);
            val name = new String(nameBuf);
            result[i] = name;
            val length = dIn.readInt();
            dIn.skipBytes(length);
        }
        return result;
    }

    public void unpack(String blobName, Path into) throws IOException {
        @Cleanup val in = data.openStream();
        @Cleanup val dIn = new DataInputStream(in);
        val count = dIn.readInt();
        for (int i = 0; i < count; i++) {
            val nameLength = dIn.readUnsignedByte();
            val nameBuf = new byte[nameLength];
            dIn.readFully(nameBuf);
            val name = new String(nameBuf);
            val length = dIn.readInt();
            if (!blobName.equals(name)) {
                dIn.skipBytes(length);
                continue;
            }
            @Cleanup val output = new BufferedOutputStream(Files.newOutputStream(into));
            copy(dIn, output, length);
            return;
        }
        throw new IOException("Blob " + blobName + " not found in pak file!");
    }

    private void copy(InputStream input, OutputStream output, int bytes) throws IOException {
        val buf = new byte[Math.min(bytes, 4096)];
        while (bytes > 0) {
            val toRead = Math.min(buf.length, bytes);
            val read = input.read(buf, 0, toRead);
            if (read < 0) {
                throw new EOFException();
            }
            output.write(buf, 0, read);
            bytes -= read;
        }
    }
}
