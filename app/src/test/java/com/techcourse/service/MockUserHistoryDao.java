package com.techcourse.service;

import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import nextstep.jdbc.DataAccessException;
import nextstep.jdbc.JdbcTemplate;

public class MockUserHistoryDao extends UserHistoryDao {

    public MockUserHistoryDao(final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public int log(final UserHistory userHistory) {
        throw new DataAccessException();
    }

    @Override
    public int log(final Connection conn, final UserHistory userHistory) {
        throw new DataAccessException();
    }
}
