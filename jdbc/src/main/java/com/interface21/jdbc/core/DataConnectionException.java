package com.interface21.jdbc.core;

public class DataConnectionException extends RuntimeException {

    public static final String DATABASE_CONNECTION_ERROR = "데이터베이스 연결에 실패했습니다. : %s";

    public DataConnectionException(Throwable cause) {
        super(String.format(DATABASE_CONNECTION_ERROR, cause.getMessage()));
    }
}
