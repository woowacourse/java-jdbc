package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcOperations;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;
import javax.sql.DataSource;

public class UserHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserHistoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(UserHistory userHistory) {
        log(jdbcTemplate::update, userHistory);
    }

    public void log(JdbcOperations jdbc, UserHistory userHistory) {
        String sql = "insert into user_history (user_id, account, password, email, created_at, created_by) "
            + "values (?, ?, ?, ?, ?, ?)";
        final int affectedRows = jdbc.update(
            sql,
            userHistory.getUserId(),
            userHistory.getAccount(),
            userHistory.getPassword(),
            userHistory.getEmail(),
            userHistory.getCreatedAt(),
            userHistory.getCreateBy()
        );
        if (affectedRows != 1) {
            throw new DataAccessException("Expected one affected history row but found " + affectedRows);
        }
    }
}
