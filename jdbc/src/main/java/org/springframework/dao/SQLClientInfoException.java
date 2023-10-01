package org.springframework.dao;

/**
 * 하나 또는 이상의 연결 정보를 가지고 데이터베이스 연결을 맺지 못했을 때 발생하는 예외입니다.
 * SQLClientInfoException에는 failedProperties라는 필드가 있어서,
 * 연결에 실패한 연결 정보를 조회할 수 있습니다.
 */
public class SQLClientInfoException extends DataAccessException {
    public SQLClientInfoException() {
        super();
    }

    public SQLClientInfoException(final String message, final Throwable cause, final boolean enableSuppression,
                                  final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SQLClientInfoException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SQLClientInfoException(final String message) {
        super(message);
    }

    public SQLClientInfoException(final Throwable cause) {
        super(cause);
    }
}
