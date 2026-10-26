package com.techcourse.service;

import com.interface21.dao.DataAccessException;

import java.sql.SQLException;

/**
 * 커밋은 완료되었지만 이후 Connection 설정 복원 또는 종료에 실패했음을 나타낸다.
 */
public class TransactionCleanupException extends DataAccessException {

    private static final long serialVersionUID = 1L;

    public TransactionCleanupException(final SQLException cause) {
        super("Password change was committed, but connection cleanup failed", cause);
    }
}
