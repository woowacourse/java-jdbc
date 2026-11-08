package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;

public class UserHistoryDao {

    private static final String INSERT_SQL =
            "INSERT INTO user_history "
                    + "(user_id, account, password, email, created_at, created_by) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        jdbcTemplate.update(INSERT_SQL, historyArgs(userHistory));
    }

    public void log(final Connection connection, final UserHistory userHistory) {
        jdbcTemplate.update(connection, INSERT_SQL, historyArgs(userHistory));
    }

    private Object[] historyArgs(final UserHistory userHistory) {
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
