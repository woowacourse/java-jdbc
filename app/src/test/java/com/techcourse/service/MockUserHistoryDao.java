package com.techcourse.service;

import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class MockUserHistoryDao extends UserHistoryDao {

    public MockUserHistoryDao(final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void log(final UserHistory userHistory) {
        throw new DataAccessException();
    }

    // TODO: 2023-10-05 임의로 추가한 아래 메서드 사용안해도 테스트 통과하게 수정 -> 즉 Connection 파라미터 받지 않도록
    @Override
    public void log(final Connection con, final UserHistory userHistory) {
        throw new DataAccessException();
    }
}
