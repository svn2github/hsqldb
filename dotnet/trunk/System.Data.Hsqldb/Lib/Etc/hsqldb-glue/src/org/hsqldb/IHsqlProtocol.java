package org.hsqldb;

/**
 *
 * @author boucherb@users
 */
public interface IHsqlProtocol {
    /**
     * Adds the given sql statement to the given direct batch request.
     * 
     * 
     * @param result to which to add the given sql statement.
     * @param sql to add.
     */
    void AddBatchDirect(Result result, String sql);

    /**
     * Adds the given parameters to the given prepared batch request.
     * 
     * 
     * @param result to which to add the given parameters.
     * @param parameters to add.
     */
    void AddBatchPrepared(Result result, Object[] parameters);

    /**
     * Clears the session attribute values.
     *
     * @param result to clear
     */
    void ClearAttributes(Result result);

    /**
     * Clears the batch values of a direct or prepared batch request.
     *
     * @param result to clear
     */
    public void ClearBatch(Result result);

    /**
     * Clears the parameter data.
     *
     * @param result to clear.
     */
    public void ClearParameterData(Result result);

    /**
     *  Creates a new request for use in retrieving session attributes.
     * 
     * 
     * @return the new request
     */
    Result CreateAttributeRequest();

    /**
     * Creates a new response suitable for holding result set data.
     * 
     * 
     * @param columnCount the number of columns in the result set.
     * @return the new response.
     */
    Result CreateDataResponse(int columnCount);

    /**
     * Creates a new response describing the given exception raised
     * while executing the given sql statement.
     * 
     * @param ex raised by executing the statement
     * @param sql whose execution raised the given exception.
     * @return the new response
     */
    Result CreateErrorResponse(Exception ex, String sql);

    /**
     * Creates a new error response with the given message, sql state and
     * error code.
     * 
     * 
     * @param message the error message
     * @param sqlState the sql state
     * @param errorCode the error code
     * @return the new response
     */
    Result CreateErrorResponse(String message, String sqlState, int errorCode);

    /**
     * Creates a new request suitable for executing direct sql batches.
     * 
     * 
     * @return the new request.
     */
    Result CreateExecuteBatchDirectRequest();

    /**
     * Creates a new request suitable for batched execution of the prepared
     * statement with the given statement identifier.
     * 
     * 
     * @param statementId the identifier of the target prepared statement.
     * @param parameterTypes the sql types of the prepared statement's parameters.
     * @return the new request.
     */
    Result CreateExecuteBatchPreparedRequest(int statementId, int[] parameterTypes);

    /**
     * Creates a new request to directly execute the given sql statement.
     * 
     * 
     * @param sql to execute.
     * @return the new request.
     */
    Result CreateExecuteDirectRequest(String sql);

    /**
     * Creates a new request to free the prepared statement
     * with the given statement identifier.
     * 
     * 
     * @param statementId of the statement to be freed
     * @return the new request
     */
    Result CreateFreeStatementRequest(int statementId);
    
    /**
     * Creates a new requsest to prepare to commit the current transaction.
     *
     * @return the new request.
     */
    Result CreatePrepareCommitRequest();

    /**
     * Creates a new request to prepare the given sql statement.
     * 
     * 
     * @param sql to prepare.
     * @return the new request.
     */
    Result CreatePrepareStatementRequest(String sql);

    /**
     * Creates a new request to release the named savepoint.
     * 
     * 
     * @param savepointName to release.
     * @return the new request.
     */
    Result CreateReleaseSavepointRequest(String savepointName);

    /**
     * Creates a new request to rollback to the named savepoint.
     * 
     * 
     * @param savepointName to which to roll back.
     * @return the new request.
     */
    Result CreateRollbackToSavepointRequest(String savepointName);

    /**
     * Creates a new request to set the named savepoint.
     * 
     * 
     * @param savepointName to set.
     * @return the new request.
     */
    Result CreateSetSavepointRequest(String savepointName);

    /**
     * Creates a new request to log in the given user using a tcp connection.
     * 
     * 
     * @param user to log in
     * @param password of the user
     * @param database to which to log in
     * @return the new request
     */
    Result CreateTcpClientLoginRequest(String user, String password, String database);

    /**
     * Creates a new response indicating that a tcp connection log in
     * was successul.
     * 
     * 
     * @param session to which the client has connected.
     * @return the new response.
     */
    Result CreateTcpClientLoginResponse(Session session);

    /**
     * Retrieves the autoCommit session attribute.
     * 
     * 
     * @param result for which to retrieve the value.
     * @return true if autoCommit, else false.
     */
    boolean GetAttributeAutoCommit(Result result);

    /**
     * Retrieves the connectionReadOnly session attribute.
     * 
     * 
     * @param result for which to retrieve the value
     * @return truu if the connection is read-only, else false.
     */
    boolean GetAttributeConnectionReadOnly(Result result);

    /**
     * Retrieves the database session attribute.
     * 
     * 
     * @param result for which to retrieve the value.
     * @return the name of the database to which the session is connected.
     */
    String GetAttributeDatabase(Result result);

    /**
     * Retrieves the databaseReadonly session attribute.
     * 
     * 
     * @param result for which to retrieve the value.
     * @return true the database to which the session is connected is read-only,
     * else false.
     */
    boolean GetAttributeDatabaseReadOnly(Result result);

    /**
     * Retrieves the isolation session attribute.
     * 
     * 
     * @param result for which to retrieve the value.
     * @return the current transaction isolation value
     */
    int GetAttributeIsolation(Result result);

    /**
     * Retrieves the type of session attribute to request or respond.
     * 
     * 
     * @param result for which to retrive the value.
     * @return the session attribute type.
     */
    int GetAttributeType(Result result);

    /**
     * Retrieves the user session attribute.
     * 
     * 
     * @param result for which to retrieve the value
     * @return the name of the current session user.
     */
    String GetAttributeUser(Result result);

    /**
     * Retrieves the sql command text
     * 
     * 
     * @param result for which to retrieve the value.
     * @return the sql command text.
     */
    String GetCommandText(Result result);

    /**
     * Retrieves the database alias.
     * 
     * 
     * @param result for which to retrieve the value.
     * @return the database alias
     */
    String GetDatabaseAlias(Result result);

    /**
     * Retrieves the database identifier.
     * 
     * 
     * @param result for which to retrieve the value.
     * @return the database identifier.
     */
    int GetDatabaseId(Result result);

    /**
     * Retrieves the type of transaction termination.
     * 
     * 
     * @param result for which to retrieve the value.
     * @return the type of transaction termination.
     */
    int GetEndTranType(Result result);

    /**
     * Retreives the error message.
     * 
     * 
     * @param result for which to retrieve the value.
     * @return the error message.
     */
    String GetErrorMessage(Result result);

    /**
     * Retrieves the maximum row count.
     * 
     * 
     * @param result for which to retrieve the value.
     * @return the maximum row count.
     */
    int GetMaxRows(Result result);

    /**
     * Retrieves the parameter data.
     * 
     * 
     * @param result for which to retrieve the value.
     * @return the parameter data.
     */
    Object[] GetParameterData(Result result);

    /**
     * Retrieves the password.
     * 
     * 
     * @param result for which to retrieve the valeu.
     * @return the password.
     */
    String GetPassword(Result result);

    /**
     * Retreives the savepoint name.
     * 
     * 
     * @param result for which to retrieve the value.
     * @return the savepoint name.
     */
    String GetSavepointName(Result result);

    /**
     * Retrieves the session identifier.
     * 
     * 
     * @param result for which to retrieve the value.
     * @return the session identifier.
     */
    int GetSessionId(Result result);

    /**
     * Retrieves the sql state.
     * 
     * 
     * @param result for which to retrieve the value.
     * @return the sql state.
     */
    String GetSqlState(Result result);

    /**
     * Retrieves the statement identifier.
     * 
     * 
     * @param result for which to retrieve the value.
     * @return the statement identifier.
     */
    int GetStatementId(Result result);

    /**
     * Retrieves the statement type.
     * 
     * 
     * @param result for which to retrieve the value.
     * @return the statement type.
     */
    int GetStatementType(Result result);

    /**
     * Retrieves the type of the request or reponse.
     * 
     * 
     * @param result for which to get the value.
     * @return the type of the request or reponse.
     */
    int GetType(Result result);

    /**
     * Retrieves the update count.
     * 
     * 
     * @param result for which to retrieve the value.
     * @return the update count.
     */
    int GetUpdateCount(Result result);

    /**
     * Retrieves the update count array.
     * 
     * 
     * @param result for which to retrieve the value.
     * @return the update count array.
     */
    int[] GetUpdateCounts(Result result);

    /**
     * Retrieves the user name string.
     * 
     * 
     * @param result for which to retrieve the value.
     * @return the user name string.
     */
    String GetUser(Result result);

    /**
     * Sets the autoCommit session attribute.
     * 
     * 
     * @param result for which to set the value
     * @param autoCommit the new value.
     */
    void SetAttributeAutoCommit(Result result, boolean autoCommit);

    /**
     * Sets the connectionReadOnly session attribute.
     * 
     * 
     * @param result for which to set the value.
     * @param readOnly the new value.
     */
    void SetAttributeConnectionReadOnly(Result result, boolean readOnly);

    /**
     * Sets the isolation session attribute.
     * 
     * 
     * @param result for which to set the value.
     * @param isolation the new value.
     */
    void SetAttributeIsolation(Result result, int isolation);

    /**
     * Sets the type of session attribute to request or respond.
     * 
     * 
     * @param result for which to set the value.
     * @param attributeType the new value.
     */
    void SetAttributeType(Result result, int attributeType);

    /**
     * Sets the sql command text.
     * 
     * 
     * @param result for which to set the value.
     * @param commandText the new value.
     */
    void SetCommandText(Result result, String commandText);

    /**
     * Sets the database alias.
     * 
     * 
     * @param result for which to retrieve the value.
     * @param databaseAlias the new value.
     */
    void SetDatabaseAlias(Result result, String databaseAlias);

    /**
     * Sets the database identifier.
     * 
     * 
     * @param result for which to set the value.
     * @param databaseId the new value.
     */
    void SetDatabaseId(Result result, int databaseId);

    /**
     * Sets the type of transaction termination.
     * 
     * 
     * @param result for which to set the value.
     * @param endTranType the new value.
     */
    void SetEndTranType(Result result, int endTranType);

    /**
     * Sets the error message.
     * 
     * 
     * @param result for which to set the value.
     * @param errorMessage the new value.
     */
    void SetErrorMessage(Result result, String errorMessage);

    /**
     * Sets the maximum row count.
     * 
     * 
     * @param result for which to set the value.
     * @param maxRows the ne value.
     */
    void SetMaxRows(Result result, int maxRows);

    /**
     * Sets the parameter data.
     * 
     * 
     * @param result for which to set the value.
     * @param data the new value.
     */
    void SetParameterData(Result result, Object[] data);

    /**
     * Sets the password.
     * 
     * 
     * @param result for which to set the value.
     * @param password the new value.
     */
    void SetPassword(Result result, String password);

    /**
     * Sets the savepoint name.
     * 
     * 
     * @param result for which to set the value.
     * @param savepointName the new value.
     */
    void SetSavepointName(Result result, String savepointName);

    /**
     * Sets the session identifier.
     * 
     * 
     * @param result for which to set the value.
     * @param sessionId the new value.
     */
    void SetSessionId(Result result, int sessionId);

    /**
     * Sets the sql state.
     * 
     * 
     * @param result for which to set the value.
     * @param sqlState the new value.
     */
    void SetSqlState(Result result, String sqlState);

    /**
     * Sets the statement identifier.
     * 
     * 
     * @param result for which to set the value.
     * @param statementId the new value.
     */
    void SetStatementId(Result result, int statementId);

    /**
     * Sets the statement type.
     * 
     * 
     * @param result for which to set the value.
     * @param statementType the new value.
     */
    void SetStatementType(Result result, int statementType);

    /**
     * Sets the type of the request or response.
     * 
     * 
     * @param result for which to set the value.
     * @param type the new value.
     */
    void SetType(Result result, int type);

    /**
     * Sets the update count.
     * 
     * 
     * @param result for which to set the value.
     * @param updateCount the new value.
     */
    void SetUpdateCount(Result result, int updateCount);

    /**
     * Sets the update count array.
     * 
     * 
     * @param result for which to set the value.
     * @param updateCounts the new value.
     */
    void SetUpdateCounts(Result result, int[] updateCounts);

    /**
     * Sets the user name string.
     * 
     * 
     * @param result for which to set the value.
     * @param user the new value.
     */
    void SetUser(Result result, String user);
    
}
