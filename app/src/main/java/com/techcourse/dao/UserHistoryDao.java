package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public class UserHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, ps -> {
            ps.setLong(1, userHistory.getUserId());
            ps.setString(2, userHistory.getAccount());
            ps.setString(3, userHistory.getPassword());
            ps.setString(4, userHistory.getEmail());
            ps.setObject(5, userHistory.getCreatedAt());
            ps.setString(6, userHistory.getCreateBy());
        });
    }
}
