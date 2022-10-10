package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.time.LocalDateTime;
import nextstep.jdbc.JdbcTemplate;

public class UserHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        final Long userId = userHistory.getUserId();
        final String account = userHistory.getAccount();
        final String password = userHistory.getPassword();
        final String email = userHistory.getEmail();
        final LocalDateTime createdAt = userHistory.getCreatedAt();
        final String createBy = userHistory.getCreateBy();

        jdbcTemplate.update(sql, userId, account, password, email, createdAt, createBy);
    }

    public void log(final Connection conn, final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        final Long userId = userHistory.getUserId();
        final String account = userHistory.getAccount();
        final String password = userHistory.getPassword();
        final String email = userHistory.getEmail();
        final LocalDateTime createdAt = userHistory.getCreatedAt();
        final String createBy = userHistory.getCreateBy();

        jdbcTemplate.update(sql, conn, userId, account, password, email, createdAt, createBy);
    }
}
