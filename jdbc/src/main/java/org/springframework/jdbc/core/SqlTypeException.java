package org.springframework.jdbc.core;

public class SqlTypeException extends RuntimeException {


    private static final String CANNOT_TRANSFER_TO_SQL_TYPE_MESSAGE = "sql로 변환할 수 없는 타입입니다.";

    public SqlTypeException() {
        super(CANNOT_TRANSFER_TO_SQL_TYPE_MESSAGE);
    }
}
