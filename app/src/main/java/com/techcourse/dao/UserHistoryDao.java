package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.time.LocalDateTime;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final Connection connection,
                    final UserHistory userHistory) {
        final String sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

        final long userId = userHistory.getUserId();
        final String account = userHistory.getAccount();
        final String password = userHistory.getPassword();
        final String email = userHistory.getEmail();
        final LocalDateTime createdAt = userHistory.getCreatedAt();
        final String createBy = userHistory.getCreateBy();

        log.debug("sql={}", sql);

        jdbcTemplate.update(connection, sql, userId, account, password, email, createdAt, createBy);
    }
}
