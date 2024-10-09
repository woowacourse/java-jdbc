package com.techcourse.dao;

import javax.sql.DataSource;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;

public class UserHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserHistoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(UserHistory userHistory) {
        String sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(
                sql,
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy()
        );
    }
}
