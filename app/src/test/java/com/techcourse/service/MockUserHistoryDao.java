package com.techcourse.service;

import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.UserHistory;
import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import javax.sql.DataSource;

public class MockUserHistoryDao extends UserHistoryDao {

    public MockUserHistoryDao(final DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void log(final Connection connection, final UserHistory userHistory) {
        super.log(connection, userHistory);
        throw new DataAccessException();
    }
}
