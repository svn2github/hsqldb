/* Copyright (c) 2001-2016, The HSQL Development Group
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


package org.hsqldb.persist;

import java.io.IOException;

import org.hsqldb.error.Error;
import org.hsqldb.error.ErrorCode;
import org.hsqldb.lib.CharArrayWriter;
import org.hsqldb.rowio.RowInputInterface;
import org.hsqldb.rowio.RowInputText;

/**
 * Reader for UTF-16 text files.
 *
 * @author Bob Preston (sqlbob@users dot sourceforge.net)
 * @author Fred Toussi (fredt@users dot sourceforge.net)
 * @version 2.3.4
 * @since 2.3.4
*/
public class TextFileReader16 implements TextFileReader {

    private RandomAccessInterface dataFile;
    private RowInputInterface     rowIn;
    private TextFileSettings      textFileSettings;
    private String                header;
    private boolean               isReadOnly;
    private CharArrayWriter       buffer;
    private long                  position = 0;

    TextFileReader16(RandomAccessInterface dataFile,
                     TextFileSettings textFileSettings,
                     RowInputInterface rowIn, boolean isReadOnly) {

        this.dataFile         = dataFile;
        this.textFileSettings = textFileSettings;
        this.rowIn            = rowIn;
        this.buffer           = new CharArrayWriter(128);

        skipBOM();
    }

    private void skipBOM() {

        try {
            if (textFileSettings.isUTF16) {
                dataFile.seek(0);

                if (dataFile.read() == 0xFE && dataFile.read() == 0xFF) {
                    position = 2;
                } else {
                    dataFile.seek(0);

                    if (dataFile.read() == 0xFF && dataFile.read() == 0xFE) {
                        position = 2;

                        textFileSettings.setLittleEndianByteOrderMark();
                    }
                }
            }
        } catch (IOException e) {
            throw Error.error(ErrorCode.TEXT_FILE_IO, e);
        }
    }

    public RowInputInterface readObject() {

        boolean hasQuote  = false;
        boolean complete  = false;
        boolean wasCR     = false;
        boolean wasNormal = false;

        buffer.reset();

        position = findNextUsedLinePos();

        if (position == -1) {
            return null;
        }

        try {
            dataFile.seek(position);

            while (!complete) {
                int c = readChar();

                wasNormal = false;

                if (c == -1) {
                    if (buffer.size() == 0) {
                        return null;
                    }

                    complete = true;

                    if (wasCR) {
                        break;
                    }

                    break;
                }

                if (c == textFileSettings.quoteChar) {
                    wasNormal = true;
                    complete  = wasCR;
                    wasCR     = false;

                    if (textFileSettings.isQuoted) {
                        hasQuote = !hasQuote;
                    }
                } else {
                    switch (c) {

                        case TextFileSettings.CR_CHAR :
                            wasCR = !hasQuote;
                            break;

                        case TextFileSettings.LF_CHAR :
                            complete = !hasQuote;
                            break;

                        default :
                            wasNormal = true;
                            complete  = wasCR;
                            wasCR     = false;
                    }
                }

                buffer.write(c);
            }

            if (complete) {
                if (wasNormal) {
                    buffer.setSize(buffer.size() - 1);
                }

                buffer.toCharArray();

                String rowString = new String(buffer.toCharArray());

                ((RowInputText) rowIn).setSource(rowString, position,
                                                 buffer.size() * 2);

                position += rowIn.getSize();

                return rowIn;
            }

            return null;
        } catch (IOException e) {
            throw Error.error(ErrorCode.TEXT_FILE_IO, e);
        }
    }

    public void readHeaderLine() {

        boolean complete  = false;
        boolean wasCR     = false;
        boolean wasNormal = false;

        buffer.reset();

        try {
            dataFile.seek(position);
        } catch (IOException e) {
            throw Error.error(ErrorCode.TEXT_FILE_IO, e);
        }

        while (!complete) {
            wasNormal = false;

            int c;

            try {
                c = readChar();

                if (c == -1) {
                    if (buffer.size() == 0) {
                        return;
                    }

                    complete = true;

                    if (!isReadOnly) {
                        dataFile.write(
                            textFileSettings.bytesForLineEnd, 0,
                            textFileSettings.bytesForLineEnd.length);
                        buffer.write(textFileSettings.NL, 0,
                                     textFileSettings.NL.length());
                    }

                    break;
                }
            } catch (IOException e) {
                throw Error.error(ErrorCode.TEXT_FILE);
            }

            switch (c) {

                case TextFileSettings.CR_CHAR :
                    wasCR = true;
                    break;

                case TextFileSettings.LF_CHAR :
                    complete = true;
                    break;

                default :
                    wasNormal = true;
                    complete  = wasCR;
                    wasCR     = false;
            }

            if (wasCR || complete) {
                continue;
            }

            buffer.write(c);
        }

        if (wasNormal) {
            buffer.setSize(buffer.size() - 1);
        }

        header   = new String(buffer.toCharArray());
        position += buffer.size() * 2;
    }

    /**
     * Searches from file pointer, pos, and finds the beginning of the first
     * line that contains any non-space character. Increments the row counter
     * when a blank line is skipped.
     *
     * If none found return -1
     */
    private long findNextUsedLinePos() {

        try {
            long    firstPos   = position;
            long    currentPos = position;
            boolean wasCR      = false;

            dataFile.seek(position);

            while (true) {
                int c = readChar();

                currentPos += 2;

                switch (c) {

                    case TextFileSettings.CR_CHAR :
                        wasCR = true;
                        break;

                    case TextFileSettings.LF_CHAR :
                        wasCR = false;

                        ((RowInputText) rowIn).skippedLine();

                        firstPos = currentPos;
                        break;

                    case ' ' :
                        if (wasCR) {
                            wasCR = false;

                            ((RowInputText) rowIn).skippedLine();
                        }
                        break;

                    case -1 :
                        return -1;

                    default :
                        if (wasCR) {
                            wasCR = false;

                            ((RowInputText) rowIn).skippedLine();
                        }

                        return firstPos;
                }
            }
        } catch (IOException e) {
            throw Error.error(ErrorCode.TEXT_FILE_IO, e);
        }
    }

    private int readChar() {

        try {
            int c1 = dataFile.read();

            if (c1 == -1) {
                return -1;
            }

            int c2 = dataFile.read();

            if (c2 == -1) {
                return -1;
            }

            if (textFileSettings.isLittleEndian) {
                int temp = c1;

                c1 = c2;
                c2 = temp;
            }

            return (char) ((c1 << 8) + c2);
        } catch (IOException e) {
            throw Error.error(ErrorCode.TEXT_FILE_IO, e);
        }
    }

    public String getHeaderLine() {
        return header;
    }

    public long getLineNumber() {
        return ((RowInputText) rowIn).getLineNumber();
    }
}
