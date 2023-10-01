package org.springframework.dao;

/**
 * 작업이 실패했으나, 애플리케이션 영역에서 복구 작업을 한 후 전체 트랜잭션을 다시 수행한다면 성공할 가능성이 있는 경우 발생하는 예외입니다.
 */
public class SQLRecoverableException extends DataAccessException {
    public SQLRecoverableException() {
        super();
    }

    public SQLRecoverableException(final String message, final Throwable cause, final boolean enableSuppression,
                                   final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SQLRecoverableException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SQLRecoverableException(final String message) {
        super(message);
    }

    public SQLRecoverableException(final Throwable cause) {
        super(cause);
    }
}
