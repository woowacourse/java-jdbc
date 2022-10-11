package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import nextstep.jdbc.JdbcTemplate;

public class UserHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int log(final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy());
    }
}
