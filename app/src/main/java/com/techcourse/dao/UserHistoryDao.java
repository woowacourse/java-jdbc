package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcOperations;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;

public class UserHistoryDao {

    private final JdbcOperations jdbcOperations;

    public UserHistoryDao(final JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    public int log(final UserHistory userHistory) {
        final String sql = "INSERT INTO user_history (user_id, account, password, email, created_at, created_by) VALUES (?, ?, ?, ?, ?, ?)";
        return jdbcOperations.update(
                sql,
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy()
        );
    }

    public int log(final UserHistory userHistory, final Connection conn) {
        final String sql = "INSERT INTO user_history (user_id, account, password, email, created_at, created_by) VALUES (?, ?, ?, ?, ?, ?)";
        return jdbcOperations.update(
                conn,
                sql,
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy()
        );
    }
}
