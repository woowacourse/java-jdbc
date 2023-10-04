package org.springframework.dao;

/**
 * 데이터 동기화에 사용되는 SyncFactory와 관련된 예외입니다.
 */
public class SyncFactoryException extends DataAccessException {
    public SyncFactoryException() {
        super();
    }

    public SyncFactoryException(final String message, final Throwable cause, final boolean enableSuppression,
                                final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SyncFactoryException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SyncFactoryException(final String message) {
        super(message);
    }

    public SyncFactoryException(final Throwable cause) {
        super(cause);
    }
}
