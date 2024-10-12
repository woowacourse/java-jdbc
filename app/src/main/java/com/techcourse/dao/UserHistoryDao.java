package com.techcourse.dao;

import javax.sql.DataSource;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapStrategy;
import com.techcourse.domain.UserHistory;

public class UserHistoryDao {

    private static final RowMapStrategy<UserHistory> USER_HISTORY_ROW_MAP_STRATEGY = resultSet -> {
        final long id = resultSet.getLong(1);
        final long userId = resultSet.getLong(2);
        final String findAccount = resultSet.getString(3);
        final String password = resultSet.getString(4);
        final String email = resultSet.getString(5);
        final String createdBy = resultSet.getString(6);
        return new UserHistory(id, userId, findAccount, password, email, createdBy);
    };

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        final String sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql, userHistory.getUserId(), userHistory.getAccount(), userHistory.getPassword(),
                userHistory.getEmail(), userHistory.getCreatedAt(), userHistory.getCreateBy());
    }

    public UserHistory findById(final long id) {
        final String sql = "SELECT id, user_id, account, password, email, created_at, created_by FROM user_history WHERE id = ?";

        return jdbcTemplate.queryForObject(sql, USER_HISTORY_ROW_MAP_STRATEGY, id);
    }
}
