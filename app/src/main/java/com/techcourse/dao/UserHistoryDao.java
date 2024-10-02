package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;
import javax.sql.DataSource;

public class UserHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserHistoryDao(DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public void log(final UserHistory userHistory) {
        String sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(
                sql,
                userHistory.getUserId(), userHistory.getAccount(), userHistory.getPassword(),
                userHistory.getEmail(), userHistory.getCreatedAt(), userHistory.getCreatedBy()
        );
    }
}
