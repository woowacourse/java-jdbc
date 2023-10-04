package org.springframework.dao;

/**
 * 같은 조건으로 재시도시 성공할 가능성이 있는 예외들의 상위 집합입니다.
 * SQLTransientException의 하위 예외로는
 * SQLTimeoutException
 * SQLTransactionRollbackException
 * SQLTransientConnectionException
 * 이 있습니다.
 */
public class SQLTransientException extends DataAccessException {
    public SQLTransientException() {
        super();
    }

    public SQLTransientException(final String message, final Throwable cause, final boolean enableSuppression,
                                 final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SQLTransientException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SQLTransientException(final String message) {
        super(message);
    }

    public SQLTransientException(final Throwable cause) {
        super(cause);
    }
}
