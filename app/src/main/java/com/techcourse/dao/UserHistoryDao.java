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

    public void log(final Connection connection, final UserHistory userHistory) {
        jdbcTemplate.insertInto("user_history")
                .value("user_id", userHistory.getUserId())
                .value("account", userHistory.getAccount())
                .value("password", userHistory.getPassword())
                .value("email", userHistory.getEmail())
                .value("created_at", userHistory.getCreatedAt())
                .value("created_by", userHistory.getCreateBy())
                .execute(connection);
    }
}
