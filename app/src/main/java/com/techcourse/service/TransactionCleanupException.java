package com.techcourse.service;

import com.interface21.dao.DataAccessException;

/**
 * 커밋은 완료됐지만 커넥션 정리에 실패했음을 나타낸다.
 */
public class TransactionCleanupException extends DataAccessException {

    private static final long serialVersionUID = 1L;

    public TransactionCleanupException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
