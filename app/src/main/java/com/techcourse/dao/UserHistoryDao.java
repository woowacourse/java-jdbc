package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public class UserHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        final String sql = "insert into user_history (user_id, account, password, email, created_at, created_by) " +
                "values (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.executeUpdate(sql,
                String.valueOf(userHistory.getUserId()),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                String.valueOf(userHistory.getCreatedAt()),
                userHistory.getCreateBy()
        );
    }
}
