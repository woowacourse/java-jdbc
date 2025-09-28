package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import com.interface21.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final JdbcTemplate jdbcTemplate;


    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (:user_id, :account, :password, :email, :created_at, :created_by)";
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", userHistory.getUserId());
        params.put("account", userHistory.getAccount());
        params.put("password", userHistory.getPassword());
        params.put("email", userHistory.getEmail());
        params.put("created_at", userHistory.getCreatedAt());
        params.put("created_by", userHistory.getCreateBy());
        jdbcTemplate.update(sql, params);
    }
}
