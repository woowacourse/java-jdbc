package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.UserHistory;
import javax.sql.DataSource;

public class MockUserHistoryDao extends UserHistoryDao {

    public MockUserHistoryDao(final DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void log(final UserHistory userHistory) {
        throw new DataAccessException();
    }
}
