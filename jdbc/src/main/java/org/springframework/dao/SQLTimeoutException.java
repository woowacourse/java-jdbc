package org.springframework.dao;

/**
 * 타임아웃과 관련된 설정값(setQueryTimeout, DriverManager.setLoginTimeout, DataSource.setLoginTimeout,XADataSource.setLoginTimeout)
 * 의 만료에 의해 발생하는 예외입니다.
 */
public class SQLTimeoutException extends DataAccessException {
    public SQLTimeoutException() {
        super();
    }

    public SQLTimeoutException(final String message, final Throwable cause, final boolean enableSuppression,
                               final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SQLTimeoutException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SQLTimeoutException(final String message) {
        super(message);
    }

    public SQLTimeoutException(final Throwable cause) {
        super(cause);
    }
}
