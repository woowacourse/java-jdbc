package com.techcourse.service;

import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.UserHistory;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public class MockUserHistoryDao extends UserHistoryDao {

    public MockUserHistoryDao(final DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void log(final UserHistory userHistory) {
        throw new DataAccessException();
    }
}
