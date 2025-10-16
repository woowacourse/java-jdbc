package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcOperations;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.UserHistory;

public class MockUserHistoryDao extends UserHistoryDao {

    public MockUserHistoryDao(final JdbcOperations jdbcOperations) {
        super(jdbcOperations);
    }

    @Override
    public int log(final UserHistory userHistory) {
        throw new DataAccessException();
    }
}
