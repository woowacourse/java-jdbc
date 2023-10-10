package com.techcourse.service;

import com.techcourse.dao.UserHistoryDaoWithTransaction;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.TransactionTemplate;

public class MockUserHistoryDao extends UserHistoryDaoWithTransaction {

    public MockUserHistoryDao(DataSource dataSource) {
        super(new TransactionTemplate(dataSource));
    }

    @Override
    public void log(final Connection connection, final UserHistory userHistory) {
        throw new DataAccessException();
    }
}
