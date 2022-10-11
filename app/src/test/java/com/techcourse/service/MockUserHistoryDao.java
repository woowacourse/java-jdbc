package com.techcourse.service;

import java.sql.Connection;

import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.UserHistory;

import nextstep.jdbc.DataAccessException;
import nextstep.jdbc.JdbcTemplate;

public class MockUserHistoryDao implements UserHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    public MockUserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void log(final UserHistory userHistory) {
        throw new DataAccessException();
    }

    @Override
    public void log(final Connection connection, final UserHistory userHistory) {
        throw new DataAccessException();
    }
}
