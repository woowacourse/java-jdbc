package com.techcourse.service;

import java.sql.Connection;

import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.UserHistory;

import nextstep.jdbc.DataAccessException;
import nextstep.jdbc.JdbcTemplate;

public class MockUserHistoryDao extends UserHistoryDao {

    public MockUserHistoryDao(final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void log(final Connection connection, final UserHistory userHistory) {
        throw new DataAccessException();
    }
}
