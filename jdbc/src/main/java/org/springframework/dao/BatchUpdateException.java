package org.springframework.dao;

/**
 * 예외 원본 번역 :
 * 배치 업데이트 작업 도중 잘생한 에러를 나타내는 예외입니다.
 * BatchUpdateException은 updateCount라는 값을 받는 특징이 있는데,
 * updateCount는 에러 발생 전까지 성공적으로 수행 완료한 배치 작업의 수를 나타냅니다.
 * updateCount의 배열 순서는 배치 작업에 배치된(..) 작업의 순서를 그대로 따라갑니다.
 */
public class BatchUpdateException extends DataAccessException {
    public BatchUpdateException() {
        super();
    }

    public BatchUpdateException(final String message, final Throwable cause, final boolean enableSuppression,
                                final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BatchUpdateException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public BatchUpdateException(final String message) {
        super(message);
    }

    public BatchUpdateException(final Throwable cause) {
        super(cause);
    }
}
