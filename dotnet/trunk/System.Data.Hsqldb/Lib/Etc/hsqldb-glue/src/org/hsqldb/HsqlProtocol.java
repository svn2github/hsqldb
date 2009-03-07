package org.hsqldb;

import org.hsqldb.lib.ArrayUtil;
import org.hsqldb.store.ValuePool;

/**
 *
 * @author boucherb@users
 */
public class HsqlProtocol implements IHsqlProtocol {

    private static final HsqlProtocol m_instance;
    

    static {
        m_instance = new HsqlProtocol();
    }

    private HsqlProtocol() {
    }

    /**
     * An HsqlProtocolInstance
     * @return
     */
    public static IHsqlProtocol GetInstance() {
        return m_instance;
    }

    /**
     * Adds the given sql statement to the given direct batch request.
     *
     * @param result to which to add the given sql statement.
     * @param sql to add.
     */
    public void AddBatchDirect(Result result, String sql) {
        result.add(new Object[]{sql});
    }

    /**
     * Adds the given parameters to the given prepared batch request.
     *
     * @param result to which to add the given parameters.
     * @param parameters to add.
     */
    public void AddBatchPrepared(Result result, Object[] parameters) {
        result.add(parameters);
    }

    /**
     * Clears the session attribute values.
     *
     * @param result to clear
     */
    public void ClearAttributes(Result result) {
        ArrayUtil.fillArray(result.rRoot.data, null);
    }

    /**
     * Clears the batch values of a direct or prepared batch request.
     *
     * @param result to clear
     */
    public void ClearBatch(Result result) {
        result.clear();
    }

    /**
     * Clears the parameter data.
     *
     * @param result to clear.
     */
    public void ClearParameterData(Result result) {
        ArrayUtil.fillArray(result.rRoot.data, null);
    }

    /**
     *  Creates a new request for use in retrieving session attributes.
     *
     * @return the new request
     */
    public Result CreateAttributeRequest() {
        Result request = new Result(ResultConstants.DATA, 7);

        request.metaData.colNames =
                request.metaData.colLabels =
                request.metaData.tableNames =
                new String[]{
                    "", "", "", "", "", "", ""
                };
        request.metaData.colTypes = new int[]{
                    Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.INTEGER,
                    Types.BOOLEAN, Types.BOOLEAN, Types.BOOLEAN
                };

        request.add(new Object[7]);

        return request;
    }

    /**
     * Creates a new response suitable for holding result set data.
     *
     * @param columnCount the number of columns in the result set.
     * @return the new response.
     */
    public Result CreateDataResponse(int columnCount) {
        return new Result(ResultConstants.DATA, columnCount);
    }

    /**
     * Creates a new response describing the given exception raised
     * while executing the given sql statement.
     * @param ex raised by executing the statement
     * @param sql whose execution raised the given exception.
     * @return the new response
     */
    public Result CreateErrorResponse(Exception ex, String sql) {
        return new Result(ex, sql);
    }

    /**
     * Creates a new error response with the given message, sql state and
     * error code.
     *
     * @param message the error message
     * @param sqlState the sql state
     * @param errorCode the error code
     * @return the new response
     */
    public Result CreateErrorResponse(String message, String sqlState, int errorCode) {
        return new Result(message, sqlState, errorCode);
    }

    /**
     * Creates a new request suitable for executing direct sql batches.
     *
     * @return the new request.
     */
    public Result CreateExecuteBatchDirectRequest() {
        return new Result(
                ResultConstants.BATCHEXECDIRECT,
                new int[]{org.hsqldb.Types.VARCHAR},
                0);
    }

    /**
     * Creates a new request suitable for batched execution of the prepared
     * statement with the given statement identifier.
     *
     * @param statementId the identifier of the target prepared statement.
     * @param parameterTypes the sql types of the prepared statement's parameters.
     * @return the new request.
     */
    public Result CreateExecuteBatchPreparedRequest(int statementId, int[] parameterTypes) {
        return new Result(
                ResultConstants.BATCHEXECUTE,
                parameterTypes,
                statementId);
    }

    /**
     * Creates a new request to directly execute the given sql statement.
     *
     * @param sql to execute.
     * @return the new request.
     */
    public Result CreateExecuteDirectRequest(String sql) {
        return Result.newExecuteDirectRequest(sql);
    }

    /**
     * Creates a new request to free the prepared statement
     * with the given statement identifier.
     *
     * @param statementId of the statement to be freed
     * @return the new request
     */
    public Result CreateFreeStatementRequest(int statementId) {
        return Result.newFreeStmtRequest(statementId);
    }

    /**
     * Creates a new requsest to prepare to commit the current transaction.
     *
     * @return the new request.
     */
    public Result CreatePrepareCommitRequest() {
        Result request = new Result(ResultConstants.SQLENDTRAN);

        SetEndTranType(request, ResultConstants.HSQLPREPARECOMMIT);

        request.mainString = "";

        return request;
    }

    /**
     * Creates a new request to prepare the given sql statement.
     *
     * @param sql to prepare.
     * @return the new request.
     */
    public Result CreatePrepareStatementRequest(String sql) {
        Result result = new Result(ResultConstants.SQLPREPARE);

        result.mainString = sql;

        return result;
    }

    /**
     * Creates a new request to release the named savepoint.
     *
     * @param savepointName to release.
     * @return the new request.
     */
    public Result CreateReleaseSavepointRequest(String savepointName) {
        return Result.newReleaseSavepointRequest(savepointName);
    }

    /**
     * Creates a new request to rollback to the named savepoint.
     *
     * @param savepointName to which to roll back.
     * @return the new request.
     */
    public Result CreateRollbackToSavepointRequest(String savepointName) {
        return Result.newRollbackToSavepointRequest(savepointName);
    }

    /**
     * Creates a new request to set the named savepoint.
     *
     * @param savepointName to set.
     * @return the new request.
     */
    public Result CreateSetSavepointRequest(String savepointName) {
        return Result.newSetSavepointRequest(savepointName);
    }

    /**
     * Creates a new request to log in the given user using a tcp connection.
     *
     * @param user to log in
     * @param password of the user
     * @param database to which to log in
     * @return the new request
     */
    public Result CreateTcpClientLoginRequest(
            String user,
            String password,
            String database) {
        Result result = new Result(ResultConstants.SQLCONNECT);

        SetUser(result, user);
        SetPassword(result, password);
        SetDatabaseAlias(result, database);

        return result;
    }

    /**
     * Creates a new response indicating that a tcp connection log in
     * was successul.
     *
     * @param session to which the client has connected.
     * @return the new response.
     */
    public Result CreateTcpClientLoginResponse(Session session) {
        Result result = new Result(ResultConstants.UPDATECOUNT);

        result.databaseID = session.getDatabase().databaseID;
        result.sessionID = session.getId();

        return result;
    }

    /**
     * Retrieves the autoCommit session attribute.
     *
     * @param result for which to retrieve the value.
     * @return true if autoCommit, else false.
     */
    public boolean GetAttributeAutoCommit(Result result) {
        return ((Boolean) result.rRoot.data[SessionInterface.INFO_AUTOCOMMIT]).booleanValue();
    }

    /**
     * Sets the autoCommit session attribute.
     *
     * @param result for which to set the value
     * @param autoCommit the new value.
     */
    public void SetAttributeAutoCommit(Result result, boolean autoCommit) {
        result.rRoot.data[SessionInterface.INFO_AUTOCOMMIT] = autoCommit
                ? Boolean.TRUE
                : Boolean.FALSE;
    }

    /**
     * Retrieves the connectionReadOnly session attribute.
     *
     * @param result for which to retrieve the value
     * @return truu if the connection is read-only, else false.
     */
    public boolean GetAttributeConnectionReadOnly(Result result) {
        return ((Boolean) result.rRoot.data[SessionInterface.INFO_CONNECTION_READONLY]).booleanValue();
    }

    /**
     * Sets the connectionReadOnly session attribute.
     *
     * @param result for which to set the value.
     * @param readOnly the new value.
     */
    public void SetAttributeConnectionReadOnly(Result result, boolean readOnly) {
        result.rRoot.data[SessionInterface.INFO_CONNECTION_READONLY] = readOnly
                ? Boolean.TRUE
                : Boolean.FALSE;
    }

    /**
     * Retrieves the database session attribute.
     *
     * @param result for which to retrieve the value.
     * @return the name of the database to which the session is connected.
     */
    public String GetAttributeDatabase(Result result) {
        return (String) result.rRoot.data[SessionInterface.INFO_DATABASE];
    }

    /**
     * Retrieves the databaseReadonly session attribute.
     *
     * @param result for which to retrieve the value.
     * @return true the database to which the session is connected is read-only,
     * else false.
     */
    public boolean GetAttributeDatabaseReadOnly(Result result) {
        return ((Boolean) result.rRoot.data[SessionInterface.INFO_DATABASE_READONLY]).booleanValue();
    }

    /**
     * Retrieves the isolation session attribute.
     *
     * @param result for which to retrieve the value.
     * @return the current transaction isolation value
     */
    public int GetAttributeIsolation(Result result) {
        return ((Integer) result.rRoot.data[SessionInterface.INFO_ISOLATION]).intValue();
    }

    /**
     * Sets the isolation session attribute.
     *
     * @param result for which to set the value.
     * @param isolation the new value.
     */
    public void SetAttributeIsolation(Result result, int isolation) {
        result.rRoot.data[SessionInterface.INFO_ISOLATION] = ValuePool.getInt(isolation);
    }

    /**
     * Retrieves the type of session attribute to request or respond.
     *
     * @param result for which to retrive the value.
     * @return the session attribute type.
     */
    public int GetAttributeType(Result result) {
        return result.updateCount;
    }

    /**
     * Sets the type of session attribute to request or respond.
     *
     * @param result for which to set the value.
     * @param attributeType the new value.
     */
    public void SetAttributeType(Result result, int attributeType) {
        result.updateCount = attributeType;
    }

    /**
     * Retrieves the user session attribute.
     *
     * @param result for which to retrieve the value
     * @return the name of the current session user.
     */
    public String GetAttributeUser(Result result) {
        return (String) result.rRoot.data[SessionInterface.INFO_USER];
    }

    /**
     * Retrieves the sql command text
     *
     * @param result for which to retrieve the value.
     * @return the sql command text.
     */
    public String GetCommandText(Result result) {
        return result.mainString;
    }

    /**
     * Sets the sql command text.
     *
     * @param result for which to set the value.
     * @param commandText the new value.
     */
    public void SetCommandText(Result result, String commandText) {
        result.mainString = commandText;
    }

    /**
     * Retrieves the database alias.
     *
     * @param result for which to retrieve the value.
     * @return the database alias
     */
    public String GetDatabaseAlias(Result result) {
        return result.subSubString;
    }

    /**
     * Sets the database alias.
     *
     * @param result for which to retrieve the value.
     * @param databaseAlias the new value.
     */
    public void SetDatabaseAlias(Result result, String databaseAlias) {
        result.subSubString = databaseAlias;
    }

    /**
     * Retrieves the database identifier.
     *
     * @param result for which to retrieve the value.
     * @return the database identifier.
     */
    public int GetDatabaseId(Result result) {
        return result.databaseID;
    }

    /**
     * Sets the database identifier.
     *
     * @param result for which to set the value.
     * @param databaseId the new value.
     */
    public void SetDatabaseId(Result result, int databaseId) {
        result.databaseID = databaseId;
    }

    /**
     * Retrieves the type of transaction termination.
     *
     * @param result for which to retrieve the value.
     * @return the type of transaction termination.
     */
    public int GetEndTranType(Result result) {
        return result.updateCount;
    }

    /**
     * Sets the type of transaction termination.
     *
     * @param result for which to set the value.
     * @param endTranType the new value.
     */
    public void SetEndTranType(Result result, int endTranType) {
        result.updateCount = endTranType;
    }

    /**
     * Retreives the error message.
     *
     * @param result for which to retrieve the value.
     * @return the error message.
     */
    public String GetErrorMessage(Result result) {
        return result.mainString;
    }

    /**
     * Sets the error message.
     *
     * @param result for which to set the value.
     * @param errorMessage the new value.
     */
    public void SetErrorMessage(Result result, String errorMessage) {
        result.mainString = errorMessage;
    }

    /**
     * Retrieves the maximum row count.
     *
     * @param result for which to retrieve the value.
     * @return the maximum row count.
     */
    public int GetMaxRows(Result result) {
        return result.updateCount;
    }

    /**
     * Sets the maximum row count.
     *
     * @param result for which to set the value.
     * @param maxRows the ne value.
     */
    public void SetMaxRows(Result result, int maxRows) {
        result.updateCount = maxRows;
    }

    /**
     * Retrieves the password.
     *
     * @param result for which to retrieve the valeu.
     * @return the password.
     */
    public String GetPassword(Result result) {
        return result.subString;
    }

    /**
     * Sets the password.
     *
     * @param result for which to set the value.
     * @param password the new value.
     */
    public void SetPassword(Result result, String password) {
        result.subString = password;
    }

    /**
     * Retrieves the parameter data.
     *
     * @param result for which to retrieve the value.
     * @return the parameter data.
     */
    public Object[] GetParameterData(Result result) {
        return result.getParameterData();
    }

    /**
     * Sets the parameter data.
     *
     * @param result for which to set the value.
     * @param data the new value.
     */
    public void SetParameterData(Result result, Object[] data) {
        result.setParameterData(data);
    }

    /**
     * Retrieves the type of the request or reponse.
     *
     * @param result for which to get the value.
     * @return the type of the request or reponse.
     */
    public int GetType(Result result) {
        return result.mode;
    }

    /**
     * Sets the type of the request or response.
     *
     * @param result for which to set the value.
     * @param type the new value.
     */
    public void SetType(Result result, int type) {
        result.mode = type;
    }

    /**
     * Retreives the savepoint name.
     *
     * @param result for which to retrieve the value.
     * @return the savepoint name.
     */
    public String GetSavepointName(Result result) {
        return result.mainString;
    }

    /**
     * Sets the savepoint name.
     *
     * @param result for which to set the value.
     * @param savepointName the new value.
     */
    public void SetSavepointName(Result result, String savepointName) {
        result.mainString = savepointName;
    }

    /**
     * Retrieves the session identifier.
     *
     * @param result for which to retrieve the value.
     * @return the session identifier.
     */
    public int GetSessionId(Result result) {
        return result.sessionID;
    }

    /**
     * Sets the session identifier.
     *
     * @param result for which to set the value.
     * @param sessionId the new value.
     */
    public void SetSessionId(Result result, int sessionId) {
        result.sessionID = sessionId;
    }

    /**
     * Retrieves the sql state.
     *
     * @param result for which to retrieve the value.
     * @return the sql state.
     */
    public String GetSqlState(Result result) {
        return result.subString;
    }

    /**
     * Sets the sql state.
     *
     * @param result for which to set the value.
     * @param sqlState the new value.
     */
    public void SetSqlState(Result result, String sqlState) {
        result.subString = sqlState;
    }

    /**
     * Retrieves the statement identifier.
     *
     * @param result for which to retrieve the value.
     * @return the statement identifier.
     */
    public int GetStatementId(Result result) {
        return result.statementID;
    }

    /**
     * Sets the statement identifier.
     *
     * @param result for which to set the value.
     * @param statementId the new value.
     */
    public void SetStatementId(Result result, int statementId) {
        result.statementID = statementId;
    }

    /**
     * Retrieves the statement type.
     *
     * @param result for which to retrieve the value.
     * @return the statement type.
     */
    public int GetStatementType(Result result) {
        return result.updateCount;
    }

    /**
     * Sets the statement type.
     *
     * @param result for which to set the value.
     * @param statementType the new value.
     */
    public void SetStatementType(Result result, int statementType) {
        result.updateCount = statementType;
    }

    /**
     * Retrieves the update count.
     *
     * @param result for which to retrieve the value.
     * @return the update count.
     */
    public int GetUpdateCount(Result result) {
        return result.updateCount;
    }

    /**
     * Sets the update count.
     *
     * @param result for which to set the value.
     * @param updateCount the new value.
     */
    public void SetUpdateCount(Result result, int updateCount) {
        result.updateCount = updateCount;
    }

    /**
     * Retrieves the update count array.
     *
     * @param result for which to retrieve the value.
     * @return the update count array.
     */
    public int[] GetUpdateCounts(Result result) {
        return result.metaData.colTypes;
    }

    /**
     * Sets the update count array.
     *
     * @param result for which to set the value.
     * @param updateCounts the new value.
     */
    public void SetUpdateCounts(Result result, int[] updateCounts) {
        result.metaData.colTypes = updateCounts;
    }

    /**
     * Retrieves the user name string.
     *
     * @param result for which to retrieve the value.
     * @return the user name string.
     */
    public String GetUser(Result result) {
        return result.mainString;
    }

    /**
     * Sets the user name string.
     *
     * @param result for which to set the value.
     * @param user the new value.
     */
    public void SetUser(Result result, String user) {
        result.mainString = user;
    }
}
