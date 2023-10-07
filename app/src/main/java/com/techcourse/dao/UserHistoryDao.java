package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final DataSource dataSource;

    public UserHistoryDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.dataSource = null;
    }

    public void log(final Connection connection, final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            pstmt.setLong(1, userHistory.getUserId());
            pstmt.setString(2, userHistory.getAccount());
            pstmt.setString(3, userHistory.getPassword());
            pstmt.setString(4, userHistory.getEmail());
            pstmt.setObject(5, userHistory.getCreatedAt());
            pstmt.setString(6, userHistory.getCreateBy());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
