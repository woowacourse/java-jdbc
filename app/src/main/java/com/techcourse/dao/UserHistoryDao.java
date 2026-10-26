package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class UserHistoryDao {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void log(final UserHistory userHistory) {
        try (Connection connection = dataSource.getConnection()) {
            log(connection, userHistory);
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public void log(final Connection connection, final UserHistory userHistory) {
        jdbcTemplate.update(
                connection,
                "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)",
                userHistory.getUserId(), userHistory.getAccount(), userHistory.getPassword(),
                userHistory.getEmail(), userHistory.getCreatedAt(), userHistory.getCreateBy()
        );
    }
}
