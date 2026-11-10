package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;

public class UserHistoryDao {

    private static final String INSERT_SQL =
        "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        jdbcTemplate.update(INSERT_SQL, params(userHistory));
    }

    private Object[] params(final UserHistory userHistory) {
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
