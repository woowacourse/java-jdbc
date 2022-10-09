package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.UserHistory;
import nextstep.jdbc.DataAccessException;
import nextstep.jdbc.JdbcTemplate;

import java.sql.Connection;

public class MockUserHistoryDao extends UserHistoryDao {

    public MockUserHistoryDao(final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void log(Connection connection, final UserHistory userHistory) {
        throw new DataAccessException();
    }
}
