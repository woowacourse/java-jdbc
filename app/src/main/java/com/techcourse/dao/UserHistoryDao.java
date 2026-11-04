package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementSetter;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);
    private static final String INSERT_SQL = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        log.debug("query : {}", INSERT_SQL);

        jdbcTemplate.update(INSERT_SQL, createLogSetter(userHistory));
    }

    public void log(final UserHistory userHistory, final Connection connection) {
        log.debug("query : {}", INSERT_SQL);

        jdbcTemplate.update(connection, INSERT_SQL, createLogSetter(userHistory));
    }

    private PreparedStatementSetter createLogSetter(final UserHistory userHistory) {
        return pstmt -> {
            pstmt.setObject(1, userHistory.getUserId());
            pstmt.setObject(2, userHistory.getAccount());
            pstmt.setObject(3, userHistory.getPassword());
            pstmt.setObject(4, userHistory.getEmail());
            pstmt.setObject(5, userHistory.getCreatedAt());
            pstmt.setObject(6, userHistory.getCreateBy());
        };
    }
}
