package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);
    private static final String INSERT_USER_HISTORY_SQL = "insert into user_history "
            + "(user_id, account, password, email, created_at, created_by) "
            + "values (?, ?, ?, ?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        log.debug("User history id : {}", userHistory.getUserId());
        jdbcTemplate.update(
                INSERT_USER_HISTORY_SQL,
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy()
        );
    }

    public void log(final Connection connection, final UserHistory userHistory) {
        log.debug("User history id : {}", userHistory.getUserId());
        jdbcTemplate.update(
                connection,
                INSERT_USER_HISTORY_SQL,
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy()
        );
    }
}
