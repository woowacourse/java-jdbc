package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.UserHistory;
import nextstep.jdbc.DataAccessException;

public class MockUserHistoryDao extends UserHistoryDao {

    public MockUserHistoryDao() {
        super(DataSourceConfig.getInstance());
    }

    @Override
    public void log(final UserHistory userHistory) {
        throw new DataAccessException();
    }
}
