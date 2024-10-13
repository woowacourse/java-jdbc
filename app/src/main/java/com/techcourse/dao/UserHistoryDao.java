package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import javax.sql.DataSource;

public class UserHistoryDao {


    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserHistoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(Connection connection, UserHistory userHistory) {
        final var sql = "INSERT INTO user_history (user_id, account, password, email, created_at, created_by) VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(
                sql,
                connection,
                userHistory.getUserId(), userHistory.getAccount(), userHistory.getPassword(),
                userHistory.getEmail(), userHistory.getCreatedAt(), userHistory.getCreateBy()
        );
    }
}
