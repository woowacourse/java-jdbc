package com.techcourse.dao;

import com.interface21.jdbc.core.ArgumentPreparedStatementSetter;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);
    private static final String INSERT_USER_HISTORY_QUERY = """
            INSERT INTO user_history (user_id, account, password, email, created_at, created_by)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(UserHistory userHistory) {
        ArgumentPreparedStatementSetter argumentPreparedStatementSetter = setUserHistoryArgument(userHistory);

        jdbcTemplate.update(INSERT_USER_HISTORY_QUERY, argumentPreparedStatementSetter);
        log.info("userHistory 업데이트에 성공했습니다.");
    }

    public void insertWithTransaction(Connection connection, UserHistory userHistory) {
        ArgumentPreparedStatementSetter argumentPreparedStatementSetter = setUserHistoryArgument(userHistory);

        jdbcTemplate.update(connection, INSERT_USER_HISTORY_QUERY, argumentPreparedStatementSetter);
        log.info("userHistory 업데이트에 성공했습니다.");
    }

    private ArgumentPreparedStatementSetter setUserHistoryArgument(UserHistory userHistory) {
        return new ArgumentPreparedStatementSetter(
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy()
        );
    }
}
