package org.springframework.dao;

/**
 * SQLState 값이 42이거나, 데이터베이스의 고유 값에 따라 발생하는 예외입니다.
 * 실행중인 쿼리가 SQL 문법에 위배되는 경우 발생하는 예외입니다.
 */
public class SQLSyntaxErrorException extends DataAccessException {
    public SQLSyntaxErrorException() {
        super();
    }

    public SQLSyntaxErrorException(final String message, final Throwable cause, final boolean enableSuppression,
                                   final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SQLSyntaxErrorException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SQLSyntaxErrorException(final String message) {
        super(message);
    }

    public SQLSyntaxErrorException(final Throwable cause) {
        super(cause);
    }
}
