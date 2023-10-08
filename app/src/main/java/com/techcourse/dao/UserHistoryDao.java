package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.UserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource = DataSourceConfig.getInstance();

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(UserHistory userHistory) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        log.info("[LOG] insert user into user_history");
        jdbcTemplate.update(connection, sql, userHistory.getUserId(), userHistory.getAccount(), userHistory.getPassword(), userHistory.getEmail(), userHistory.getCreatedAt(), userHistory.getCreateBy());
    }
}
