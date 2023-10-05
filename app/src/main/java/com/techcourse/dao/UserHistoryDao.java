package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Connection;
import java.util.List;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);
    private static final RowMapper<UserHistory> rowMapper = rs -> {
        Long id = rs.getLong("id");
        long userId = rs.getLong("user_id");
        String account = rs.getString("account");
        String password = rs.getString("password");
        String email = rs.getString("email");
        String createdBy = rs.getString("created_by");

        return new UserHistory(id, userId, account, password, email, createdBy);
    };

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(Connection connection, UserHistory userHistory) {
        jdbcTemplate.execute(connection, "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)",
                userHistory.getUserId(), userHistory.getAccount(), userHistory.getPassword(), userHistory.getEmail(), userHistory.getCreatedAt(), userHistory.getCreateBy());
    }

    public List<UserHistory> findAll() {
        return jdbcTemplate.query("select id, user_id, account, password, email, created_by from user_history", rowMapper);
    }
}
