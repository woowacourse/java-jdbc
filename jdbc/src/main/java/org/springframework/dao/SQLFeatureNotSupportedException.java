package org.springframework.dao;

/**
 * SQLState 값이 '0A'일 때 발생하는 예외입니다.
 * JDBC 드라이버가 특정 JDBC 기능을 지원하지 않을 때 발생합니다.
 */
public class SQLFeatureNotSupportedException extends DataAccessException {
    public SQLFeatureNotSupportedException() {
        super();
    }

    public SQLFeatureNotSupportedException(final String message, final Throwable cause, final boolean enableSuppression,
                                           final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SQLFeatureNotSupportedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SQLFeatureNotSupportedException(final String message) {
        super(message);
    }

    public SQLFeatureNotSupportedException(final Throwable cause) {
        super(cause);
    }
}
