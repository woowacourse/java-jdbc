package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.UserHistory;

import java.sql.Connection;

public class MockUserHistoryDao extends UserHistoryDao {

    public MockUserHistoryDao(final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public long log(final Connection connection, UserHistory userHistory) {
        throw new DataAccessException();
    }
}
