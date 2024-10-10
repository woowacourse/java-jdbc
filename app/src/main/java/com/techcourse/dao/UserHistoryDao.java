package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, (preparedStatement) -> {
            preparedStatement.setObject(1, userHistory.getUserId());
            preparedStatement.setObject(2, userHistory.getAccount());
            preparedStatement.setObject(3, userHistory.getPassword());
            preparedStatement.setObject(4, userHistory.getEmail());
            preparedStatement.setObject(5, userHistory.getCreatedAt());
            preparedStatement.setObject(5, userHistory.getCreateBy());
        });
    }
}
