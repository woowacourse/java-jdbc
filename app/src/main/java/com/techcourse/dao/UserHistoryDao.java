package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementSetter;
import com.techcourse.domain.UserHistory;

public class UserHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        PreparedStatementSetter pss = ps -> {
            ps.setLong(1, userHistory.getUserId());
            ps.setString(2, userHistory.getAccount());
            ps.setString(3, userHistory.getPassword());
            ps.setString(4, userHistory.getEmail());
            ps.setObject(5, userHistory.getCreatedAt());
            ps.setString(6, userHistory.getCreateBy());
        };

        jdbcTemplate.update(sql, pss);
    }
}
