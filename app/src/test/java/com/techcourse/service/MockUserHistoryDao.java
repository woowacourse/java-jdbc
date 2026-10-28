package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.UserHistory;

public class MockUserHistoryDao extends UserHistoryDao {

    public MockUserHistoryDao(final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    // 이력 저장 단계에서 의도적으로 실패시켜, 앞서 실행된 비밀번호 변경이 롤백되는지 확인한다.
    @Override
    public void log(final UserHistory userHistory) {
        throw new DataAccessException();
    }
}
