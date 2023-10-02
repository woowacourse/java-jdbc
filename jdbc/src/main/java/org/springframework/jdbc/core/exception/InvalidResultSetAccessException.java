package org.springframework.jdbc.core.exception;

/**
 * ResultSet이 잘못된 방식으로 접근되었을 때 발생하는 예외.
 * 이는 일반적으로 잘못된 ResultSet 열 인덱스 또는 이름이 지정되었을 때 발생합니다.
 * 또한 연결이 끊긴 SqlRowSets에 의해 발생되기도 합니다.
 */
public class InvalidResultSetAccessException extends RuntimeException {
    public InvalidResultSetAccessException(final String reason) {
        super(reason);
    }

    public InvalidResultSetAccessException() {
        super();
    }

    public InvalidResultSetAccessException(final String reason, final Throwable cause) {
        super(reason, cause);
    }
}
