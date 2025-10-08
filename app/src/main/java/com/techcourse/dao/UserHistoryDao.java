package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import com.interface21.jdbc.core.JdbcTemplate;

public class UserHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, ps -> {
            ps.setObject(1, userHistory.getUserId());
            ps.setObject(2, userHistory.getAccount());
            ps.setObject(3, userHistory.getPassword());
            ps.setObject(4, userHistory.getEmail());
            ps.setObject(5, userHistory.getCreatedAt());
            ps.setObject(6, userHistory.getCreateBy());
        });
    }
}
