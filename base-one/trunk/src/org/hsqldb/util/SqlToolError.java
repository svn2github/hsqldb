package org.hsqldb.util;

public class SqlToolError extends Exception {
    public SqlToolError(Exception e) {
        super(e.getMessage());
    }
}
