package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private static final String INSERT_SQL = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void log(final UserHistory userHistory) {
        final var affectedRows = jdbcTemplate.update(INSERT_SQL, toParameters(userHistory));
        log.debug("User history saved, userId={}, affectedRows={}", userHistory.getUserId(), affectedRows);
    }

    public void log(final Connection connection, final UserHistory userHistory) {
        final var affectedRows = jdbcTemplate.update(connection, INSERT_SQL, toParameters(userHistory));
        log.debug("User history saved, userId={}, affectedRows={}", userHistory.getUserId(), affectedRows);
    }

    private Object[] toParameters(final UserHistory userHistory) {
        return new Object[]{
                userHistory.getUserId(), userHistory.getAccount(), userHistory.getPassword(),
                userHistory.getEmail(), userHistory.getCreatedAt(), userHistory.getCreateBy()
        };
    }
}
