package com.techcourse.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(final UserHistory userHistory) {
        log.info("[UserHistoryDao] createWithTransactional: {}", userHistory.toString());
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, userHistory.getUserId(), userHistory.getAccount(), userHistory.getPassword(), userHistory.getEmail(), userHistory.getCreatedAt(), userHistory.getCreateBy());
    }
}
