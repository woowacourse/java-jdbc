package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        jdbcTemplate.insertInto("user_history")
                .value("user_id", userHistory.getUserId())
                .value("account", userHistory.getAccount())
                .value("password", userHistory.getPassword())
                .value("email", userHistory.getEmail())
                .value("created_at", userHistory.getCreatedAt())
                .value("created_by", userHistory.getCreateBy())
                .execute();
    }

    public void log(final Connection conn, final UserHistory userHistory) {
        final String sql = "INSERT INTO user_history (user_id, account, password, email, created_at, created_by) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(conn, sql,
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy()
        );
    }
}
