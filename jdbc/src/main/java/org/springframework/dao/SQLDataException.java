package org.springframework.dao;

/**
 * SQLState(데이터베이스 관련 예외의 유형과 원인을 식별하는 표준화된 5자리 식별자) 값이 22이거나 그 아래일 때 발생하는 예외입니다. 데이터 변환 뿐 아니라, 0으로 나누거나 잘못된 함수 호출 등 다양한
 * 원인에 의해 발생할 수 있습니다. 애플리케이션에서 사용하는 각 데이터베이스 벤더사 문서를 찾아 정확한 원인을 파악할 수 있습니다.
 */
public class SQLDataException extends DataAccessException {
    public SQLDataException() {
        super();
    }

    public SQLDataException(final String message, final Throwable cause, final boolean enableSuppression,
                            final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SQLDataException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SQLDataException(final String message) {
        super(message);
    }

    public SQLDataException(final Throwable cause) {
        super(cause);
    }
}
