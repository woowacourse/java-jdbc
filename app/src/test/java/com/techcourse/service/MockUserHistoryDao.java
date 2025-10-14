package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcOperations;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;

public class MockUserHistoryDao extends UserHistoryDao {

    public MockUserHistoryDao(final JdbcOperations jdbcOperations) {
        super(jdbcOperations);
    }

    @Override
    public int log(final UserHistory userHistory) {
        throw new DataAccessException();
    }

    @Override
    public int log(final UserHistory userHistory, final Connection conn) {
        throw new DataAccessException();
    }
}
