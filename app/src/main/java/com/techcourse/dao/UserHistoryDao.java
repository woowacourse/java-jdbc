package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import java.time.LocalDateTime;
import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

        Long userId = userHistory.getUserId();
        String account = userHistory.getAccount();
        String password = userHistory.getPassword();
        String email = userHistory.getEmail();
        LocalDateTime createdAt = userHistory.getCreatedAt();
        String createBy = userHistory.getCreateBy();
        jdbcTemplate.update(sql, userId, account, password, email, createdAt, createBy);
    }
}
