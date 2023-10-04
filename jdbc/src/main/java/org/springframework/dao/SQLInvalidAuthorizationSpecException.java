package org.springframework.dao;

/**
 * SQLState 값이 28이거나, 데이터베이스의 고유 값에 따라 발생하는 예외입니다.
 * 연결 설정 중 사용된 인가 정보가 유효하지 않을 때 발생하는 예외입니다.
 */
public class SQLInvalidAuthorizationSpecException extends DataAccessException {
    public SQLInvalidAuthorizationSpecException() {
        super();
    }

    public SQLInvalidAuthorizationSpecException(final String message, final Throwable cause,
                                                final boolean enableSuppression,
                                                final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SQLInvalidAuthorizationSpecException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SQLInvalidAuthorizationSpecException(final String message) {
        super(message);
    }

    public SQLInvalidAuthorizationSpecException(final Throwable cause) {
        super(cause);
    }
}
