package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import nextstep.jdbc.JdbcTemplate;

public class UserHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

        final var userId = userHistory.getUserId();
        final var account = userHistory.getAccount();
        final var password = userHistory.getPassword();
        final var email = userHistory.getEmail();
        final var createdAt = userHistory.getCreatedAt();
        final var createBy = userHistory.getCreateBy();
        jdbcTemplate.update(sql, userId, account, password, email, createdAt, createBy);
    }

    public void log(final Connection connection, final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

        final var userId = userHistory.getUserId();
        final var account = userHistory.getAccount();
        final var password = userHistory.getPassword();
        final var email = userHistory.getEmail();
        final var createdAt = userHistory.getCreatedAt();
        final var createBy = userHistory.getCreateBy();
        jdbcTemplate.update(connection, sql, userId, account, password, email, createdAt, createBy);
    }
}
