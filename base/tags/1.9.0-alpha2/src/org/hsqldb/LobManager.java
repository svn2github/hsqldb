/* Copyright (c) 2001-2009, The HSQL Development Group
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

import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.hsqldb.lib.HashMappedList;
import org.hsqldb.lib.HsqlByteArrayInputStream;
import org.hsqldb.lib.HsqlByteArrayOutputStream;
import org.hsqldb.lib.LineGroupReader;
import org.hsqldb.navigator.RowSetNavigator;
import org.hsqldb.persist.LobStore;
import org.hsqldb.persist.LobStoreMem;
import org.hsqldb.persist.LobStoreRAFile;
import org.hsqldb.result.Result;
import org.hsqldb.result.ResultLob;
import org.hsqldb.result.ResultMetaData;
import org.hsqldb.types.BlobData;
import org.hsqldb.types.BlobDataID;
import org.hsqldb.types.ClobData;
import org.hsqldb.types.ClobDataID;

/**
 * @author Fred Toussi (fredt@users dot sourceforge.net)
 * @version 1.9.0
 * @since 1.9.0
 */
public class LobManager {

    Database database;
    LobStore lobStore;

    //
    int lobBlockSize         = 1024 * 32;
    int totalBlockLimitCount = 1024 * 1024 * 1024;

    //
    String   resourceFileName = "/org/hsqldb/resources/lob-schema.sql";
    String[] starters         = new String[]{ "/*" };

    //
    Statement getLob;
    Statement getLobPart;
    Statement deleteLob;
    Statement deleteLobPart;
    Statement divideLobPart;
    Statement createLob;
    Statement createLobPart;
    Statement setLobLength;
    Statement getNextLobId;

    // LOBS columns
    private interface LOBS {

        int BLOCK_ADDR   = 0;
        int BLOCK_COUNT  = 1;
        int BLOCK_OFFSET = 2;
        int LOB_ID       = 3;
    }

    private interface ALLOC_BLOCKS {

        int BLOCK_COUNT  = 0;
        int BLOCK_OFFSET = 1;
        int LOB_ID       = 2;
    }

    //BLOCK_ADDR INT, BLOCK_COUNT INT, TX_ID BIGINT
    private static String initialiseBlocksSQL =
        "INSERT INTO SYSTEM_LOBS.BLOCKS VALUES(?,?,?)";
    private static String getLobSQL =
        "SELECT * FROM SYSTEM_LOBS.LOB_IDS WHERE LOB_ID = ?";
    private static String getLobPartSQL =
        "SELECT * FROM SYSTEM_LOBS.LOBS WHERE LOB_ID = ? AND BLOCK_OFFSET >= ? AND BLOCK_OFFSET < ? ORDER BY BLOCK_OFFSET";

    // DELETE_BLOCKS(L_ID BIGINT, B_OFFSET INT, B_COUNT INT, TX_ID BIGINT)
    private static String deleteLobPartSQL =
        "CALL SYSTEM_LOBS.DELETE_BLOCKS(?,?,?,?)";
    private static String createLobSQL =
        "INSERT INTO SYSTEM_LOBS.LOB_IDS VALUES(?, ?, ?)";
    private static String updateLobLengthSQL =
        "UPDATE SYSTEM_LOBS.LOB_IDS SET LOB_LENGTH = ? WHERE LOB_ID = ?";
    private static String createLobPartSQL =
        "CALL SYSTEM_LOBS.ALLOC_BLOCKS(?, ?, ?)";
    private static String divideLobPartSQL =
        "CALL SYSTEM_LOBS.DIVIDE_BLOCK(?, ?)";
    private static String getSpanningBlockSQL =
        "SELECT * FROM SYSTEM_LOBS.LOBS WHERE LOB_ID = ? AND ? > BLOCK_OFFSET AND ? < BLOCK_OFFSET + BLOCK_COUNT";
    private static String getNextLobIdSQL =
        "VALUES NEXT VALUE FOR SYSTEM_LOBS.LOB_ID";
    private static String deleteLobSQL = "CALL SYSTEM_LOBS.DELETE_LOB(?, ?)";

    //    (OUT L_ADDR INT, IN B_COUNT INT, IN B_OFFSET INT, IN L_ID BIGINT, IN L_LENGTH BIGINT)
    public LobManager(Database database) {
        this.database = database;
    }

    void createSchema() throws HsqlException {

        Session           session = database.sessionManager.getSysSession();
        InputStream fis = getClass().getResourceAsStream(resourceFileName);
        InputStreamReader reader  = null;

        try {
            reader = new InputStreamReader(fis, "ISO-8859-1");
        } catch (Exception e) {}

        LineNumberReader lineReader = new LineNumberReader(reader);
        LineGroupReader  lg = new LineGroupReader(lineReader, starters);
        HashMappedList   map        = lg.getAsMap();
        String           sql = (String) map.get("/*lob_schema_definition*/");
        Statement        statement  = session.compileStatement(sql);

//            database.logger.stopLogging();
        Result result = statement.execute(session, null);
        Table table = database.schemaManager.getTable(session, "BLOCKS",
            "SYSTEM_LOBS");

//            table.isTransactional = false;
//            database.logger.restartLogging();
        session.commit(false);

        getLob        = session.compileStatement(getLobSQL);
        getLobPart    = session.compileStatement(getLobPartSQL);
        createLob     = session.compileStatement(createLobSQL);
        createLobPart = session.compileStatement(createLobPartSQL);
        divideLobPart = session.compileStatement(divideLobPartSQL);
        deleteLob     = session.compileStatement(deleteLobSQL);
        deleteLobPart = session.compileStatement(deleteLobPartSQL);
        setLobLength  = session.compileStatement(updateLobLengthSQL);
        getNextLobId  = session.compileStatement(getNextLobIdSQL);
    }

    void initialiseLobSpace() throws HsqlException {

        Session   session   = database.sessionManager.getSysSession();
        Statement statement = session.compileStatement(initialiseBlocksSQL);
        Object[]  args      = new Object[3];

        args[0] = Integer.valueOf(0);
        args[1] = Integer.valueOf(totalBlockLimitCount);
        args[2] = Long.valueOf(0);

        session.executeCompiledStatement(statement, args);
        session.commit(false);
    }

    void initializeLobStore() throws HsqlException {}

    void open() throws HsqlException {

        if (DatabaseURL.isFileBasedDatabaseType(database.getType())) {
            lobStore = new LobStoreRAFile(database);
        } else {
            lobStore = new LobStoreMem();
        }

//        lobStore = new LobMemStore();
    }

    void close() {}

    long getNewLobID(Session session) {

        Result result = getNextLobId.execute(session, null);

        if (result.isError()) {
            return 0;
        }

        RowSetNavigator navigator = result.getNavigator();
        boolean         next      = navigator.next();

        if (!next) {
            navigator.close();

            return 0;
        }

        Object[] data = (Object[]) navigator.getCurrent();

        return ((Long) data[0]).longValue();
    }

    Object[] getLobHeader(Session session, long lobID) {

        ResultMetaData meta     = getLob.getParametersMetaData();
        Object         params[] = new Object[meta.getColumnCount()];

        params[0] = Long.valueOf(lobID);

        Result result = getLob.execute(session, params);

        if (result.isError()) {
            return null;
        }

        RowSetNavigator navigator = result.getNavigator();
        boolean         next      = navigator.next();

        if (!next) {
            navigator.close();

            return null;
        }

        Object[] data = (Object[]) navigator.getCurrent();

        return data;
    }

    BlobData getBlob(Session session, long lobID) {

        Object[] data = getLobHeader(session, lobID);

        if (data == null) {
            return null;
        }

        BlobData blob = new BlobDataID(lobID);

        return blob;
    }

    ClobData getClob(Session session, long lobID) {

        Object[] data = getLobHeader(session, lobID);

        if (data == null) {
            return null;
        }

        ClobData clob = new ClobDataID(lobID);

        return clob;
    }

    public long createBlob(Session session) {

        long           lobID    = getNewLobID(session);
        ResultMetaData meta     = createLob.getParametersMetaData();
        Object         params[] = new Object[meta.getColumnCount()];

        params[0] = Long.valueOf(lobID);
        params[1] = Long.valueOf(0);
        params[2] = Integer.valueOf(Types.SQL_BLOB);

        Result result = session.executeCompiledStatement(createLob, params);

        return lobID;
    }

    public long createClob(Session session) {

        long           lobID    = getNewLobID(session);
        ResultMetaData meta     = createLob.getParametersMetaData();
        Object         params[] = new Object[meta.getColumnCount()];

        params[0] = Long.valueOf(lobID);
        params[1] = Long.valueOf(0);
        params[2] = Integer.valueOf(Types.SQL_CLOB);

        Result result = session.executeCompiledStatement(createLob, params);

        return lobID;
    }

    public void deleteLob(Session session, long lobID) {

        ResultMetaData meta     = deleteLob.getParametersMetaData();
        Object         params[] = new Object[meta.getColumnCount()];

        params[0] = Long.valueOf(lobID);
        params[1] = Long.valueOf(session.transactionTimestamp);

        Result result = session.executeCompiledStatement(createLob, params);
    }

    public Result getLength(Session session, long lobID) {

        Object[] data = getLobHeader(session, lobID);

        if (data == null) {
            Result.newErrorResult(Error.error(ErrorCode.X_22522));
        }

        long length = ((Long) data[1]).longValue();

        return ResultLob.newLobSetResponse(lobID, length);
    }

    /** @todo - implement copying the lob */
    public Result getLob(Session session, long lobID, long offset,
                         long length) {

        Object[] data = getLobHeader(session, lobID);

        if (data == null) {
            Result.newErrorResult(Error.error(ErrorCode.X_22522));
        }

        long lobLength = ((Long) data[1]).longValue();
        int  lobType   = ((Integer) data[1]).intValue();
        long newID;

        if (lobType == Types.SQL_BLOB) {
            newID = createBlob(session);
        } else {
            lobLength *= 2;
            newID     = createClob(session);
        }

        int newBlockCount = (int) length / lobBlockSize;

        if (length % lobBlockSize != 0) {
            newBlockCount++;
        }

        createBlockAddresses(session, lobID, 0, newBlockCount);

        // copy the contents
        return ResultLob.newLobSetResponse(newID, length);
    }

    public Result getChars(Session session, long lobID, long offset,
                           int length) {

        Result result = getBytes(session, lobID, offset * 2, length * 2);

        if (result.isError()) {
            return result;
        }

        byte[]                   bytes = ((ResultLob) result).getByteArray();
        HsqlByteArrayInputStream be    = new HsqlByteArrayInputStream(bytes);
        char[]                   chars = new char[bytes.length / 2];

        try {
            for (int i = 0; i < chars.length; i++) {
                chars[i] = be.readChar();
            }
        } catch (Exception e) {
            return Result.newErrorResult(e);
        }

        return ResultLob.newLobGetCharsResponse(lobID, offset, chars);
    }

    public Result getBytes(Session session, long lobID, long offset,
                           int length) {

        int blockOffset     = (int) (offset / lobBlockSize);
        int byteBlockOffset = (int) (offset % lobBlockSize);
        int blockLimit      = (int) ((offset + length) / lobBlockSize);
        int byteLimitOffset = (int) ((offset + length) % lobBlockSize);

        if (byteLimitOffset == 0) {
            byteLimitOffset = lobBlockSize;
        } else {
            blockLimit++;
        }

        int    dataBytesPosition = 0;
        byte[] dataBytes         = new byte[length];
        int[][] blockAddresses = getBlockAddresses(session, lobID,
            blockOffset, blockLimit);

        if (blockAddresses.length == 0) {
            return Result.newErrorResult(Error.error(ErrorCode.X_22522));
        }

        //
        int i = 0;
        int blockCount = blockAddresses[i][1]
                         - (blockAddresses[i][2] - blockOffset);

        if (blockAddresses[i][1] + blockAddresses[i][2] > blockLimit) {
            blockCount -= (blockAddresses[i][1] + blockAddresses[i][2]
                           - blockLimit);
        }

        byte[] bytes;

        try {
            bytes = lobStore.getBlockBytes(lobID,
                                           blockAddresses[i][0] + blockOffset,
                                           blockCount);
        } catch (HsqlException e) {
            return Result.newErrorResult(e);
        }

        int subLength = lobBlockSize * blockCount - byteBlockOffset;

        if (subLength > length) {
            subLength = length;
        }

        System.arraycopy(bytes, byteBlockOffset, dataBytes, dataBytesPosition,
                         subLength);

        dataBytesPosition += subLength;

        i++;

        for (; i < blockAddresses.length && dataBytesPosition < length; i++) {
            blockCount = blockAddresses[i][1];

            if (blockAddresses[i][1] + blockAddresses[i][2] > blockLimit) {
                blockCount -= (blockAddresses[i][1] + blockAddresses[i][2]
                               - blockLimit);
            }

            try {
                bytes = lobStore.getBlockBytes(lobID, blockAddresses[i][0],
                                               blockCount);
            } catch (HsqlException e) {
                return Result.newErrorResult(e);
            }

            subLength = lobBlockSize * blockCount;

            if (subLength > length - dataBytesPosition) {
                subLength = length - dataBytesPosition;
            }

            System.arraycopy(bytes, 0, dataBytes, dataBytesPosition,
                             subLength);

            dataBytesPosition += subLength;
        }

        return ResultLob.newLobGetBytesResponse(lobID, offset, dataBytes);
    }

    public Result setBytesBA(Session session, long lobID, byte[] dataBytes,
                             long offset, int length) {

        Object[] data = getLobHeader(session, lobID);

        if (data == null) {
            return Result.newErrorResult(Error.error(ErrorCode.X_22522));
        }

        long oldLength       = ((Long) data[1]).longValue();
        int  blockOffset     = (int) (offset / lobBlockSize);
        int  byteBlockOffset = (int) (offset % lobBlockSize);
        int  blockLimit      = (int) ((offset + length) / lobBlockSize);
        int  byteLimitOffset = (int) ((offset + length) % lobBlockSize);

        if (byteLimitOffset == 0) {
            byteLimitOffset = lobBlockSize;
        } else {
            blockLimit++;
        }

        int[][] blockAddresses = getBlockAddresses(session, lobID,
            blockOffset, blockLimit);
        byte[] newBytes = new byte[(blockLimit - blockOffset) * lobBlockSize];

        if (blockAddresses.length > 0) {
            int blockAddress = blockAddresses[0][0]
                               + (blockOffset - blockAddresses[0][2]);

            try {
                byte[] block = lobStore.getBlockBytes(lobID, blockAddress, 1);

                System.arraycopy(block, 0, newBytes, 0, lobBlockSize);

                if (blockAddresses.length > 1) {
                    blockAddress =
                        blockAddresses[blockAddresses.length - 1][0]
                        + (blockLimit
                           - blockAddresses[blockAddresses.length - 1][2] - 1);
                    block = lobStore.getBlockBytes(lobID, blockAddress, 1);

                    System.arraycopy(block, 0, newBytes,
                                     blockLimit - blockOffset - 1,
                                     lobBlockSize);
                } else if (blockLimit - blockOffset > 1) {
                    blockAddress = blockAddresses[0][0]
                                   + (blockLimit - blockAddresses[0][2] - 1);
                    block = lobStore.getBlockBytes(lobID, blockAddress, 1);

                    System.arraycopy(block, 0, newBytes,
                                     (blockLimit - blockOffset - 1)
                                     * lobBlockSize, lobBlockSize);
                }
            } catch (HsqlException e) {
                return Result.newErrorResult(e);
            }

            // should turn into SP
            divideBlockAddresses(session, lobID, blockOffset);
            divideBlockAddresses(session, lobID, blockLimit);
            deleteBlockAddresses(session, lobID, blockOffset, blockLimit);
        }

        createBlockAddresses(session, lobID, blockOffset,
                             blockLimit - blockOffset);
        System.arraycopy(dataBytes, 0, newBytes, byteBlockOffset, length);

        blockAddresses = getBlockAddresses(session, lobID, blockOffset,
                                           blockLimit);

        //
        try {
            for (int i = 0; i < blockAddresses.length; i++) {
                lobStore.setBlockBytes(lobID, newBytes, blockAddresses[i][0],
                                       blockAddresses[i][1]);
            }
        } catch (HsqlException e) {
            return Result.newErrorResult(e);
        }

        if (offset + length > oldLength) {
            oldLength = offset + length;

            setLength(session, lobID, oldLength);
        }

        return ResultLob.newLobSetResponse(lobID, 0);
    }

    private Result setBytesDI(Session session, long lobID,
                              DataInput dataInput, long length) {

        int blockLimit      = (int) (length / lobBlockSize);
        int byteLimitOffset = (int) (length % lobBlockSize);

        if (byteLimitOffset == 0) {
            byteLimitOffset = lobBlockSize;
        } else {
            blockLimit++;
        }

        createBlockAddresses(session, lobID, 0, blockLimit);

        int[][] blockAddresses = getBlockAddresses(session, lobID, 0,
            blockLimit);
        byte[] dataBytes = new byte[lobBlockSize];

        for (int i = 0; i < blockAddresses.length; i++) {
            for (int j = 0; j < blockAddresses[i][1]; j++) {
                int localLength = lobBlockSize;

                if (i == blockAddresses.length - 1
                        && j == blockAddresses[i][1] - 1) {
                    localLength = byteLimitOffset;

                    for (int k = localLength; k < lobBlockSize; k++) {
                        dataBytes[k] = 0;
                    }
                }

                try {
                    dataInput.readFully(dataBytes, 0, localLength);
                } catch (IOException e) {

                    // deallocate
                    return Result.newErrorResult(e);
                }

                try {
                    lobStore.setBlockBytes(lobID, dataBytes,
                                           blockAddresses[i][0] + j, 1);
                } catch (HsqlException e) {
                    return Result.newErrorResult(e);
                }
            }
        }

        return ResultLob.newLobSetResponse(lobID, 0);
    }

    public Result setBytes(Session session, long lobID, byte[] dataBytes,
                           long offset) {

        if (dataBytes.length == 0) {
            return ResultLob.newLobSetResponse(lobID, 0);
        }

        Object[] data = getLobHeader(session, lobID);

        if (data == null) {
            return Result.newErrorResult(Error.error(ErrorCode.X_22522));
        }

        long length = ((Long) data[1]).longValue();
        Result result = setBytesBA(session, lobID, dataBytes, offset,
                                   dataBytes.length);

        if (offset + dataBytes.length > length) {
            setLength(session, lobID, offset + dataBytes.length);
        }

        return result;
    }

    public Result setBytes(Session session, long lobID, DataInput dataInput,
                           long length) throws HsqlException {

        if (length == 0) {
            return ResultLob.newLobSetResponse(lobID, 0);
        }

        Result result = setBytesDI(session, lobID, dataInput, length);

        setLength(session, lobID, length);

        return result;
    }

    public Result setChars(Session session, long lobID, long offset,
                           char[] chars) {

        if (chars.length == 0) {
            return ResultLob.newLobSetResponse(lobID, 0);
        }

        Object[] data = getLobHeader(session, lobID);

        if (data == null) {
            return Result.newErrorResult(Error.error(ErrorCode.X_22522));
        }

        long length = ((Long) data[1]).longValue();
        HsqlByteArrayOutputStream os =
            new HsqlByteArrayOutputStream(chars.length * 2);

        os.write(chars, 0, chars.length);

        Result result = setBytesBA(session, lobID, os.getBuffer(), offset * 2,
                                   os.getBuffer().length);

        if (result.isError()) {
            return result;
        }

        if (offset + chars.length > length) {
            result = setLength(session, lobID, offset + chars.length);

            if (result.isError()) {
                return result;
            }
        }

        return ResultLob.newLobSetResponse(lobID, 0);
    }

    public Result setChars(Session session, long lobID, long offset,
                           DataInput dataInput,
                           long length) throws HsqlException {

        if (length == 0) {
            return ResultLob.newLobSetResponse(lobID, 0);
        }

        Result result = setBytes(session, lobID, dataInput, length * 2);

        if (result.isError()) {
            return result;
        }

        setLength(session, lobID, length);

        return ResultLob.newLobSetResponse(lobID, 0);
    }

    public Result truncate(Session session, long lobID, long offset) {

        Object[] data = getLobHeader(session, lobID);

        if (data == null) {
            return Result.newErrorResult(Error.error(ErrorCode.X_22522));
        }

        /** @todo 1.9.0 - double offset for clob */
        long length          = ((Long) data[1]).longValue();
        int  blockOffset     = (int) (offset / lobBlockSize);
        int  blockLimit      = (int) ((offset + length) / lobBlockSize);
        int  byteLimitOffset = (int) ((offset + length) % lobBlockSize);

        if (byteLimitOffset != 0) {
            blockLimit++;
        }

        ResultMetaData meta     = deleteLobPart.getParametersMetaData();
        Object         params[] = new Object[meta.getColumnCount()];

        params[0] = Long.valueOf(lobID);
        params[1] = Integer.valueOf(blockOffset);
        params[2] = Integer.valueOf(blockLimit);
        params[3] = Long.valueOf(session.transactionTimestamp);

        Result result = session.executeCompiledStatement(deleteLobPart,
            params);

        setLength(session, lobID, offset);

        return ResultLob.newLobTruncateResponse(lobID);
    }

    public Result setLength(Session session, long lobID, long length) {

        ResultMetaData meta     = setLobLength.getParametersMetaData();
        Object         params[] = new Object[meta.getColumnCount()];

        params[0] = Long.valueOf(length);
        params[1] = Long.valueOf(lobID);

        Result result = session.executeCompiledStatement(setLobLength, params);

        return result;
    }

    int[][] getBlockAddresses(Session session, long lobID, int offset,
                              int limit) {

        ResultMetaData meta     = getLobPart.getParametersMetaData();
        Object         params[] = new Object[meta.getColumnCount()];

        params[0] = Long.valueOf(lobID);
        params[1] = Integer.valueOf(offset);
        params[2] = Integer.valueOf(limit);

        Result          result    = getLobPart.execute(session, params);
        RowSetNavigator navigator = result.getNavigator();
        int             size      = navigator.getSize();
        int[][]         blocks    = new int[size][3];

        for (int i = 0; i < size; i++) {
            navigator.absolute(i);

            Object[] data = (Object[]) navigator.getCurrent();

            blocks[i][0] = ((Integer) data[LOBS.BLOCK_ADDR]).intValue();
            blocks[i][1] = ((Integer) data[LOBS.BLOCK_COUNT]).intValue();
            blocks[i][2] = ((Integer) data[LOBS.BLOCK_OFFSET]).intValue();
        }

        navigator.close();

        return blocks;
    }

    void deleteBlockAddresses(Session session, long lobID, int offset,
                              int count) {

        ResultMetaData meta     = deleteLobPart.getParametersMetaData();
        Object         params[] = new Object[meta.getColumnCount()];

        params[0] = Long.valueOf(lobID);
        params[1] = Integer.valueOf(offset);
        params[2] = Integer.valueOf(count);

        Result result = session.executeCompiledStatement(deleteLobPart,
            params);
    }

    void divideBlockAddresses(Session session, long lobID, int offset) {

        ResultMetaData meta     = divideLobPart.getParametersMetaData();
        Object         params[] = new Object[meta.getColumnCount()];

        params[0] = Long.valueOf(lobID);
        params[1] = Integer.valueOf(offset);

        Result result = session.executeCompiledStatement(divideLobPart,
            params);
    }

    void createBlockAddresses(Session session, long lobID, int offset,
                              int count) {

        ResultMetaData meta     = createLobPart.getParametersMetaData();
        Object         params[] = new Object[meta.getColumnCount()];

        params[ALLOC_BLOCKS.BLOCK_COUNT]  = Integer.valueOf(count);
        params[ALLOC_BLOCKS.BLOCK_OFFSET] = Integer.valueOf(offset);
        params[ALLOC_BLOCKS.LOB_ID]       = Long.valueOf(lobID);

        Result result = session.executeCompiledStatement(createLobPart,
            params);
    }
}
