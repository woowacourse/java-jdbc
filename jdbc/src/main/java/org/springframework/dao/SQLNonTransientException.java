package org.springframework.dao;

/**
 * 예외 발생 원인을 제거하지 않는 이상 재시도해도 똑같이 발생하는 예외들의 상위 집합입니다.
 * 해당 예외의 하위에 속하는 예외들로는
 * SQLDataException
 * SQLFeatureNotSupportedException
 * SQLIntegrityConstraintViolationException
 * SQLInvalidAuthorizationSpecException
 * SQLNonTransientConnectionException
 * SQLSyntaxErrorException
 * 이 있습니다.
 */
public class SQLNonTransientException extends DataAccessException {
    public SQLNonTransientException() {
        super();
    }

    public SQLNonTransientException(final String message, final Throwable cause, final boolean enableSuppression,
                                    final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SQLNonTransientException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SQLNonTransientException(final String message) {
        super(message);
    }

    public SQLNonTransientException(final Throwable cause) {
        super(cause);
    }
}
