package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.dsl.DslJdbcTemplate;
import com.techcourse.domain.UserHistory;

public class UserHistoryDao {

    private final DslJdbcTemplate jdbc;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbc = new DslJdbcTemplate(jdbcTemplate);
    }

    public void log(final UserHistory userHistory) {
        jdbc.insert("user_history")
                .of("user_id", userHistory.getUserId())
                .of("account", userHistory.getAccount())
                .of("password", userHistory.getPassword())
                .of("email", userHistory.getEmail())
                .of("created_at", userHistory.getCreatedAt())
                .of("created_by", userHistory.getCreateBy())
                .execute();
    }
}
