package com.techcourse.service;

import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.UserHistory;
import nextstep.jdbc.JdbcTemplate;
import nextstep.jdbc.exception.DataAccessException;

public class MockUserHistoryDao extends UserHistoryDao {

    public MockUserHistoryDao(final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public Long log(final UserHistory userHistory) {
        throw new DataAccessException();
    }
}
