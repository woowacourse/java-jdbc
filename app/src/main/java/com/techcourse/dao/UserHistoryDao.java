package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementSetter;

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

    public void log(final UserHistory userHistory) {
        jdbcTemplate.update(INSERT_SQL, createPreparedStatementSetter(userHistory));
    }

    private PreparedStatementSetter createPreparedStatementSetter(final UserHistory userHistory) {
        return statement -> {
            statement.setLong(1, userHistory.getUserId());
            statement.setString(2, userHistory.getAccount());
            statement.setString(3, userHistory.getPassword());
            statement.setString(4, userHistory.getEmail());
            statement.setObject(5, userHistory.getCreatedAt());
            statement.setString(6, userHistory.getCreateBy());
        };
    }
}
