package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;

public class UserHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, preparedStatement ->{
            preparedStatement.setLong(1,userHistory.getUserId());
            preparedStatement.setString(2,userHistory.getAccount());
            preparedStatement.setString(3,userHistory.getPassword());
            preparedStatement.setString(4,userHistory.getEmail());
            preparedStatement.setString(5,userHistory.getCreatedAt().toString());
            preparedStatement.setString(6,userHistory.getCreateBy());
        });
    }
}
