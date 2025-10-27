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
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Unpacker {
    private final URL data;
    public Unpacker(URL data) {
        this.data = data;
    }

    public String[] names() throws IOException {
        @Cleanup val in = data.openStream();
        @Cleanup val zipIn = new ZipInputStream(in);
        ZipEntry entry;
        val result = new ArrayList<String>();
        while ((entry = zipIn.getNextEntry()) != null) {
            result.add(entry.getName());
        }
        return result.toArray(new String[0]);
    }

    public void unpack(String blobName, Path into) throws IOException {
        @Cleanup val in = data.openStream();
        @Cleanup val zipIn = new ZipInputStream(in);
        ZipEntry entry;
        while ((entry = zipIn.getNextEntry()) != null) {
            if (entry.getName().equals(blobName)) {
                val size = entry.getSize();
                @Cleanup val output = new BufferedOutputStream(Files.newOutputStream(into));
                copy(zipIn, output, size);
                return;
            }
        }
        throw new IOException("Blob " + blobName + " not found in pak file!");
    }

    private void copy(InputStream input, OutputStream output, long bytes) throws IOException {
        val buf = new byte[(int) Math.min(bytes, 4096)];
        while (bytes > 0) {
            val toRead = (int)Math.min(buf.length, bytes);
            val read = input.read(buf, 0, toRead);
            if (read < 0) {
                throw new EOFException();
            }
            output.write(buf, 0, read);
            bytes -= read;
        }
    }
}
