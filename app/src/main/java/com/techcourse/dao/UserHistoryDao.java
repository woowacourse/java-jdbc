package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final Connection connection, final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        try (final var ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userHistory.getUserId());
            ps.setString(2, userHistory.getAccount());
            ps.setString(3, userHistory.getPassword());
            ps.setString(4, userHistory.getEmail());
            ps.setTimestamp(5, Timestamp.valueOf(userHistory.getCreatedAt()));
            ps.setString(6, userHistory.getCreateBy());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void log(final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(
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
