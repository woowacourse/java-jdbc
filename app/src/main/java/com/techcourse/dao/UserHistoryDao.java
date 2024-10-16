package com.techcourse.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementSetter;
import com.techcourse.domain.UserHistory;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        final PreparedStatementSetter preparedStatementSetter = pstmt -> {
            pstmt.setLong(1, userHistory.getUserId());
            pstmt.setString(2, userHistory.getAccount());
            pstmt.setString(3, userHistory.getPassword());
            pstmt.setString(4, userHistory.getEmail());
            pstmt.setObject(5, userHistory.getCreatedAt());
            pstmt.setString(6, userHistory.getCreateBy());
        };
        jdbcTemplate.update(sql, preparedStatementSetter);
    }
}
