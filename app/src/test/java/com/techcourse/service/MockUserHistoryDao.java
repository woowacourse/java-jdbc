package com.techcourse.service;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.dao.UserHistoryDao;

public class MockUserHistoryDao extends UserHistoryDao {

    public MockUserHistoryDao(final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }
}
