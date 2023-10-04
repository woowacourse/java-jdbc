package org.springframework.dao;

/**
 * SQLState가 23이거나 데이터베이스 벤더의 특정 값일 때 발생하는 예외입니다.
 * 기본키 또는 외래키의 무결성 제약을 위배했을 때 발생하는 예외입니다.
 */
public class SQLIntegrityConstraintViolationException extends DataAccessException {
    public SQLIntegrityConstraintViolationException() {
        super();
    }

    public SQLIntegrityConstraintViolationException(final String message, final Throwable cause,
                                                    final boolean enableSuppression,
                                                    final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SQLIntegrityConstraintViolationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SQLIntegrityConstraintViolationException(final String message) {
        super(message);
    }

    public SQLIntegrityConstraintViolationException(final Throwable cause) {
        super(cause);
    }
}
