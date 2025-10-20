package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.UserHistory;
import com.interface21.dao.DataAccessException;
import java.sql.Connection;

public class MockUserHistoryDao extends UserHistoryDao {

    public MockUserHistoryDao() {
        super(DataSourceConfig.getInstance());
    }

    @Override
    public void log(Connection connection, final UserHistory userHistory) {
        throw new DataAccessException();
    }
}
