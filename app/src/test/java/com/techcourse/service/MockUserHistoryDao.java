package com.techcourse.service;

import java.sql.Connection;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.UserHistory;

public class MockUserHistoryDao extends UserHistoryDao {

    public MockUserHistoryDao(final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void log(Connection con, UserHistory userHistory) throws DataAccessException {
        throw new DataAccessException();
    }
}
