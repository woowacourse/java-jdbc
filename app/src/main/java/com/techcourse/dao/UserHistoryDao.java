package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate1) {
        this.jdbcTemplate = jdbcTemplate1;
    }

    public void log(final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.handleQuery(sql, (final PreparedStatement pstmt) -> {
            try {
                pstmt.setLong(1, userHistory.getUserId());
                pstmt.setString(2, userHistory.getAccount());
                pstmt.setString(3, userHistory.getPassword());
                pstmt.setString(4, userHistory.getEmail());
                pstmt.setObject(5, userHistory.getCreatedAt());
                pstmt.setString(6, userHistory.getCreateBy());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
