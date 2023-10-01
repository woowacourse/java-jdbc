package org.springframework.dao;

/**
 * 예외 원본 번역 :
 * MaxFieldSize를 초과하는 등의 이유로 삽입할 데이터가 예치지 않게 잘려나갔을 때 발생하는 예외입니다.
 */
public class DataTruncation extends DataAccessException {
    public DataTruncation() {
        super();
    }

    public DataTruncation(final String message, final Throwable cause, final boolean enableSuppression,
                          final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public DataTruncation(final String message, final Throwable cause) {
        super(message, cause);
    }

    public DataTruncation(final String message) {
        super(message);
    }

    public DataTruncation(final Throwable cause) {
        super(cause);
    }
}
