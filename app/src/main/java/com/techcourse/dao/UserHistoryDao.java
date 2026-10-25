package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;
import javax.sql.DataSource;

public class UserHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

        Object[] args = new Object[6];
        args[0] = userHistory.getUserId();
        args[1] = userHistory.getAccount();
        args[2] = userHistory.getPassword();
        args[3] = userHistory.getEmail();
        args[4] = userHistory.getCreatedAt();
        args[5] = userHistory.getCreateBy();

        jdbcTemplate.update(sql, args);
    }
}
