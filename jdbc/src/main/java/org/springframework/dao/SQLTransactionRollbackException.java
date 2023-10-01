package org.springframework.dao;

/**
 * SQLState 값이 40이거나, 각 데이터베이스 벤더사의 고유 상황에 따라 발생하는 예외입니다.
 * 데드락과 같은 트랜잭션 동기화 실패로 인해 트랜잭션이 자동으로 롤백되었을 때 발생하는 예외입니다.
 */
public class SQLTransactionRollbackException extends DataAccessException {
    public SQLTransactionRollbackException() {
        super();
    }

    public SQLTransactionRollbackException(final String message, final Throwable cause, final boolean enableSuppression,
                                           final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SQLTransactionRollbackException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SQLTransactionRollbackException(final String message) {
        super(message);
    }

    public SQLTransactionRollbackException(final Throwable cause) {
        super(cause);
    }
}
