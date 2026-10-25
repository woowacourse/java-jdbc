package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
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
        jdbcTemplate.update(INSERT_SQL, toArgs(userHistory));
    }

    public void log(final Connection connection, final UserHistory userHistory) {
        jdbcTemplate.update(connection, INSERT_SQL, toArgs(userHistory));
    }

    private Object[] toArgs(final UserHistory userHistory) {
        return new Object[]{
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy()
        };
    }
}
