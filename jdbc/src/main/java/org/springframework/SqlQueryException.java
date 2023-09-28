package org.springframework;

public class SqlQueryException extends RuntimeException {

    private static final String WRONG_QUERY_ERROR_MESSAGE_FORMAT = "SQL Query error: %s \nSQL: %s";

    public SqlQueryException(final String message, final String query) {
        super(String.format(WRONG_QUERY_ERROR_MESSAGE_FORMAT, message, query));
    }


}
