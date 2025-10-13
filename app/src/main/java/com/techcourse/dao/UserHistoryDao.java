package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcOperations;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;

public class UserHistoryDao {

    private static final String INSERT_SQL = "INSERT INTO user_history (user_id, account, password, email, created_at, created_by) VALUES (?, ?, ?, ?, ?, ?)";

    private final JdbcOperations jdbcOperations;

    public UserHistoryDao(final JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    public int log(final UserHistory userHistory) {
        return jdbcOperations.update(INSERT_SQL, toParams(userHistory));
    }

    public int log(final UserHistory userHistory, final Connection conn) {
        return jdbcOperations.update(conn, INSERT_SQL, toParams(userHistory));
    }

    private Object[] toParams(final UserHistory userHistory) {
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
