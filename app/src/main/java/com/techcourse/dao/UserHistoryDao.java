package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import com.interface21.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.util.Objects;

public class UserHistoryDao {

    private static final String INSERT_SQL = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate must not be null");
    }

    public void log(final UserHistory userHistory) {
        jdbcTemplate.update(INSERT_SQL, logParameters(userHistory));
    }

    public void log(final Connection connection, final UserHistory userHistory) {
        jdbcTemplate.update(connection, INSERT_SQL, logParameters(userHistory));
    }

    private Object[] logParameters(final UserHistory userHistory) {
        return new Object[]{
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy()
        };
    }
}
