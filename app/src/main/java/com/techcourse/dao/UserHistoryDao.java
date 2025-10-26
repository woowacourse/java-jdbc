package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;

public class UserHistoryDao {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.dataSource = null;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy());
    }

    public void log(final Connection connection, final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setObject(1, userHistory.getUserId());
            pstmt.setObject(2, userHistory.getAccount());
            pstmt.setObject(3, userHistory.getPassword());
            pstmt.setObject(4, userHistory.getEmail());
            pstmt.setObject(5, userHistory.getCreatedAt());
            pstmt.setObject(6, userHistory.getCreateBy());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
