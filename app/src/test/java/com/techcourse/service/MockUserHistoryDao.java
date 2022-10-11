package com.techcourse.service;

import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.JdbcTemplate;

public class MockUserHistoryDao extends UserHistoryDao {

    public MockUserHistoryDao(final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void log(Connection connection, UserHistory userHistory) {
        throw new DataAccessException();
    }
}
