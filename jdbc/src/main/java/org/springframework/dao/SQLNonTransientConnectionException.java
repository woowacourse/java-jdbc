package org.springframework.dao;

/**
 * SQLNonTransientException 중 커넥션 연결과 관련된 예외입니다.
 */
public class SQLNonTransientConnectionException extends DataAccessException {
    public SQLNonTransientConnectionException() {
        super();
    }

    public SQLNonTransientConnectionException(final String message, final Throwable cause,
                                              final boolean enableSuppression,
                                              final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SQLNonTransientConnectionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SQLNonTransientConnectionException(final String message) {
        super(message);
    }

    public SQLNonTransientConnectionException(final Throwable cause) {
        super(cause);
    }
}
