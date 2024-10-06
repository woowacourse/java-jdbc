package com.techcourse.service;

import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.UserHistory;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;

public class MockUserHistoryDao extends UserHistoryDao {

    public MockUserHistoryDao(final JdbcTemplate jdbcTemplate) {
        super();
    }

    @Override
    public void log(final UserHistory userHistory) {
        throw new DataAccessException();
    }
}
