package org.springframework.dao;

/**
 * RowSet(데이터베이스 결과 집합(ResultSet)을 자바 객체로 표현하고 다루기 위한 인터페이스)와 관련된 예외입니다.
 */
public class RowSetWarning extends DataAccessException {
    public RowSetWarning() {
        super();
    }

    public RowSetWarning(final String message, final Throwable cause, final boolean enableSuppression,
                         final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public RowSetWarning(final String message, final Throwable cause) {
        super(message, cause);
    }

    public RowSetWarning(final String message) {
        super(message);
    }

    public RowSetWarning(final Throwable cause) {
        super(cause);
    }
}
