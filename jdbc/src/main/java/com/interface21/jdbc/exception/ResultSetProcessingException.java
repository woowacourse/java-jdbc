package com.interface21.jdbc.exception;

public class ResultSetProcessingException extends JdbcException {
    public ResultSetProcessingException(Throwable cause, String sql) {
        super("Failed while iterating ResultSet", cause, sql);
    }
}
