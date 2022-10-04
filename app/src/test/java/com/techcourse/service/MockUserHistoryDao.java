package com.techcourse.service;

import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.UserHistory;
import nextstep.jdbc.dao.DataAccessException;
import nextstep.jdbc.core.JdbcTemplate;

public class MockUserHistoryDao extends UserHistoryDao {

    public MockUserHistoryDao(final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void log(final UserHistory userHistory) {
        throw new DataAccessException();
    }
}
