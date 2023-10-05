package com.techcourse.service;

import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.TransactionManager;

public class MockUserHistoryDao extends UserHistoryDao {

    public MockUserHistoryDao(final TransactionManager transactionManager, final JdbcTemplate jdbcTemplate) {
        super(transactionManager, jdbcTemplate);
    }

    @Override
    public void log(final Connection connection, final UserHistory userHistory) {
        throw new DataAccessException();
    }
}
