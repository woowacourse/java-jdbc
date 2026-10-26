package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import javax.sql.DataSource;

public class UserHistoryDao {

    private final String INSERT_USER_HISTORY_SQL = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        jdbcTemplate.update(INSERT_USER_HISTORY_SQL, userHistory.getUserId(), userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(), userHistory.getCreatedAt(), userHistory.getCreateBy());
    }

    public void log(final Connection conn, final UserHistory userHistory) {
        jdbcTemplate.update(conn, INSERT_USER_HISTORY_SQL, userHistory.getUserId(), userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(), userHistory.getCreatedAt(), userHistory.getCreateBy());
    }
}
