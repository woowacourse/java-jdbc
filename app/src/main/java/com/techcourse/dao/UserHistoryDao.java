package com.techcourse.dao;

import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.UserHistory;
import com.interface21.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);
    private static final RowMapper<UserHistory> ROW_MAPPER = rs -> new UserHistory(
            rs.getLong("id"),
            rs.getLong("userId"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email"),
            rs.getString("createBy")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, userHistory.getUserId(), userHistory.getAccount(), userHistory.getPassword(), userHistory.getEmail(), userHistory.getCreatedAt(), userHistory.getCreateBy());
    }

    public void log(final Connection connection, final UserHistory userHistory) throws SQLException {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(connection, sql, userHistory.getUserId(), userHistory.getAccount(), userHistory.getPassword(), userHistory.getEmail(), userHistory.getCreatedAt(), userHistory.getCreateBy());
    }
}
