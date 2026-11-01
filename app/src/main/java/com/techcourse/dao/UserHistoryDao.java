package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;

import java.util.List;

public class UserHistoryDao {

    private static final RowMapper<UserHistory> USER_HISTORY_ROW_MAPPER = rs -> new UserHistory(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getString("created_by"));

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy());
    }

    public List<UserHistory> findAllByUserId(final long userId) {
        final var sql = "select id, user_id, account, password, email, created_at, created_by from user_history where user_id = ?";
        return jdbcTemplate.query(sql, USER_HISTORY_ROW_MAPPER, userId);
    }
}
