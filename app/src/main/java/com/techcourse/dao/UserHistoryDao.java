package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;

import javax.sql.DataSource;

public class UserHistoryDao {
    private static final String INSERT_SQL = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory history) {
        jdbcTemplate.update(INSERT_SQL, history.getUserId(), history.getAccount(), history.getPassword(),
                history.getEmail(), history.getCreatedAt(), history.getCreateBy());
    }

}
