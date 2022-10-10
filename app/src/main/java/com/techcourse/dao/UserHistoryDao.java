package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import nextstep.jdbc.JdbcTemplate;

public class UserHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        final String sql = "insert into user_history "
                + "(user_id, account, password, email, created_at, created_by) "
                + "values (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, (statement) -> {
            statement.setLong(1, userHistory.getUserId());
            statement.setString(2, userHistory.getAccount());
            statement.setString(3, userHistory.getPassword());
            statement.setString(4, userHistory.getEmail());
            statement.setObject(5, userHistory.getCreatedAt());
            statement.setString(6, userHistory.getCreateBy());
        });
    }
}
