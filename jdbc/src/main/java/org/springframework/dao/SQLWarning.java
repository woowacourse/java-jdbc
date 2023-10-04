package org.springframework.dao;

/**
 * 데이터베이스 연결 과정에서 발생하는 예외입니다.
 * Connection, Statement, ResultSet으로부터 발생할 수 있습니다.
 */
public class SQLWarning extends DataAccessException {
    public SQLWarning() {
    }

    public SQLWarning(final String message, final Throwable cause, final boolean enableSuppression,
                      final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SQLWarning(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SQLWarning(final String message) {
        super(message);
    }

    public SQLWarning(final Throwable cause) {
        super(cause);
    }
}
