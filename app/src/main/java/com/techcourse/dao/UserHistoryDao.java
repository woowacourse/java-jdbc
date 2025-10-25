package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.ConnectionProvider;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final ConnectionProvider connectionProvider;

    public UserHistoryDao(final DataSource dataSource, final ConnectionProvider connectionProvider) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.connectionProvider = connectionProvider;
    }

    public void log(final UserHistory userHistory) {
        Connection connection = connectionProvider.getConnection();
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(connection, sql,
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy()
        );
    }
}
