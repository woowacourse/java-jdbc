package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;

public class UserHistoryDao {

    private static final String SQL_INSERT = """
            insert into user_history (
                user_id,
                account,
                password,
                email,
                created_at,
                created_by)
            values (?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        jdbcTemplate.update(
                SQL_INSERT,
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy()
        );
    }
}
