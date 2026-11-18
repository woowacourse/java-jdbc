package com.techcourse.service;

import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.UserHistory;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;

import java.sql.Connection;

public class MockUserHistoryDao extends UserHistoryDao {

    public MockUserHistoryDao(final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void log(final UserHistory userHistory) {
        throw new DataAccessException();
    }

    @Override
    public void log(final Connection connection, final UserHistory userHistory) {
        // 이력 INSERT 후 실패시켜 두 작업이 함께 롤백되는지 검증한다.
        super.log(connection, userHistory);
        throw new DataAccessException("Failed after writing user history");
    }
}
