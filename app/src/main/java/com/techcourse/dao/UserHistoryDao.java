package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;

public class UserHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(UserHistory userHistory) {
        String sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(
                sql,
                userHistory.userId(),
                userHistory.account(),
                userHistory.password(),
                userHistory.email(),
                userHistory.createdAt(),
                userHistory.createdBy()
        );
    }

    public void log(Connection conn, UserHistory userHistory) {
        String sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(
                conn,
                sql,
                userHistory.userId(),
                userHistory.account(),
                userHistory.password(),
                userHistory.email(),
                userHistory.createdAt(),
                userHistory.createdBy()
        );
    }
}
