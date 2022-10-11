package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import java.time.LocalDateTime;
import javax.sql.DataSource;
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

        final long userId = userHistory.getUserId();
        final String account = userHistory.getAccount();
        final String password = userHistory.getPassword();
        final String email = userHistory.getEmail();
        final LocalDateTime createdAt = userHistory.getCreatedAt();
        final String createBy = userHistory.getCreateBy();

        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.executeUpdate(sql, userId, account, password, email, createdAt, createBy);
    }
}
