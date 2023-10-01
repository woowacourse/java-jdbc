package org.springframework.dao;

/**
 * BLOB, CLOB, STRUCT같은 SQL 타입 또는 DATALINK나 JAVAOBJECT같은 배열 자료를 직렬화 또는 역직렬화하는 과정에서 발생하는 예외입니다.
 */
public class SerialException extends DataAccessException {
    public SerialException() {
        super();
    }

    public SerialException(final String message, final Throwable cause, final boolean enableSuppression,
                           final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SerialException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SerialException(final String message) {
        super(message);
    }

    public SerialException(final Throwable cause) {
        super(cause);
    }
}
