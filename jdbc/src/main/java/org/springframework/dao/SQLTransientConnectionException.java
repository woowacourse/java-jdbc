package org.springframework.dao;

/**
 * SQLState 값이 08 또는 데이터베이스 벤더 고유의 값에 따라 발생하는 예외입니다.
 * 데이터베이스 연결에 실패했을 경우 발생하는 예외입니다. 연결에 실패했을 때 애플리케이션 레벨에서 이 예외를 발생하지 않는 경우, 연결에 실패했는데도 성공한 것으로 간주될 수 있습니다.
 */
public class SQLTransientConnectionException extends DataAccessException {
    public SQLTransientConnectionException() {
        super();
    }

    public SQLTransientConnectionException(final String message, final Throwable cause, final boolean enableSuppression,
                                           final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SQLTransientConnectionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SQLTransientConnectionException(final String message) {
        super(message);
    }

    public SQLTransientConnectionException(final Throwable cause) {
        super(cause);
    }
}
