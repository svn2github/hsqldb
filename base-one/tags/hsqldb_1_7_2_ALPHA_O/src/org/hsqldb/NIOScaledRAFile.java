/* Copyright (c) 2001-2002, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG, 
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.hsqldb;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * NIO version or DatabaseFile.This class is used only for storing a CACHED
 * TABLE .data file and cannot be used for TEXT TABLE source files.
 *
 * @author fredt@users
 * @version  1.7.2
 * @since 1.7.2
 */
class NIOScaledRAFile extends ScaledRAFile {

    MappedByteBuffer buffer;
    FileChannel      channel;
    long             fileLength;
    int              INITIAL_FILE_LENGTH = 0x100000;

    public NIOScaledRAFile(String name, boolean mode,
                           int multiplier)
                           throws FileNotFoundException, IOException {

        super(name, mode, multiplier);

        channel    = file.getChannel();
        fileLength = INITIAL_FILE_LENGTH;

        enlargeBuffer(file.length());
    }

    private void enlargeBuffer(long newPos) throws IOException {

        System.out.println("NIO next enlargeBuffer()");

        int position = 0;

        if (buffer != null) {
            position = buffer.position();

            buffer.force();
        }

        while (fileLength < newPos) {
            fileLength *= 2;
        }

        buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, fileLength);

        buffer.position(position);
    }

    long length() throws IOException {
        return fileLength;
    }

    public void seek(long newPos) throws IOException {

        if (newPos > fileLength) {
            enlargeBuffer(newPos);
        }

        try {
            buffer.position((int) newPos);
        } catch (Exception e) {
            System.out.println(newPos);
            e.printStackTrace();
        }
    }

    public long getFilePointer() throws IOException {
        return (buffer.position() + scale - 1) / scale;
    }

    public int read() throws IOException {
        return buffer.get();
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int offset, int length) throws IOException {

        buffer.get(b, offset, length);

        return length;
    }

    public int readInt() throws IOException {
        return buffer.getInt();
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len) throws IOException {

        if ((long) buffer.position() + len > fileLength) {
            enlargeBuffer((long) buffer.position() + len);
        }

        buffer.put(b, off, len);
    }

    public void writeInt(int i) throws IOException {

        if ((long) buffer.position() + 4 > fileLength) {
            enlargeBuffer((long) buffer.position() + 4);
        }

        buffer.putInt(i);
    }

    public void close() throws IOException {

        System.out.println("NIO next close() - fileLength = " + fileLength);
        System.out.println("NIO next buffer.force()");
        buffer.force();

        buffer = null;

        System.out.println("NIO next channel.close()");
        channel.close();

        channel = null;

        System.out.println("NIO next file.close()");
        file.close();
    }
}
