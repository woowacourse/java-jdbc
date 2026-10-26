package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import com.interface21.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public class UserHistoryDao {

    private static final String INSERT_SQL = """
            insert into user_history
            (user_id, account, password, email, created_at, created_by)
            values (?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        jdbcTemplate.update(INSERT_SQL, parameters(userHistory));
    }

    private Object[] parameters(UserHistory userHistory) {
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
