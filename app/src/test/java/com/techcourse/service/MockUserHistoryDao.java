package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.UserHistory;

public class MockUserHistoryDao extends UserHistoryDao {

    public MockUserHistoryDao(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void log(UserHistory userHistory) {
        throw new DataAccessException("테스트 중입니다");
    }
}
