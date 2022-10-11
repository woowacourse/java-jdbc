package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import nextstep.jdbc.DataAccessException;
import nextstep.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final JdbcTemplate jdbcTemplate;


    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory, Connection connection) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        try {
            jdbcTemplate.update(sql, connection, userHistory.getUserId(), userHistory.getAccount(),
                userHistory.getPassword(), userHistory.getEmail(), userHistory.getCreatedAt(),
                userHistory.getCreateBy());
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
        }
    }
}
