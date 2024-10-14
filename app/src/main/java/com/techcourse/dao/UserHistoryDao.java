package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementSetter;
import com.techcourse.domain.UserHistory;
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

        log.debug("query : {}", sql);

        jdbcTemplate.update(sql, getPreparedStatementSetter(userHistory));
    }

    private PreparedStatementSetter getPreparedStatementSetter(UserHistory userHistory) {
        return preparedStatement -> {
            preparedStatement.setLong(1, userHistory.getUserId());
            preparedStatement.setString(2, userHistory.getAccount());
            preparedStatement.setString(3, userHistory.getPassword());
            preparedStatement.setString(4, userHistory.getEmail());
            preparedStatement.setObject(5, userHistory.getCreatedAt());
            preparedStatement.setString(6, userHistory.getCreateBy());
        };
    }
}
