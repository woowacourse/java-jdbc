package org.springframework.dao;

/**
 * SyncProvider라는, Java에서 데이터 동시과흫 관리하는 인터페이스 사용 도중 발생하는 예외합니다.
 * 주로 데이터 동기화 작업 중 발생하는 문제나 오류를 나타내며, 데이터베이스나 다른 데이터 저장소 간의 동기화 작업에서 발생할 수 있습니다.
 */
public class SyncProviderException extends DataAccessException {
    public SyncProviderException() {
        super();
    }

    public SyncProviderException(final String message, final Throwable cause, final boolean enableSuppression,
                                 final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SyncProviderException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SyncProviderException(final String message) {
        super(message);
    }

    public SyncProviderException(final Throwable cause) {
        super(cause);
    }
}
