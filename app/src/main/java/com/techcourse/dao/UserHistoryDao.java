package com.techcourse.dao;

import com.interface21.jdbc.core.Parameters;
import com.techcourse.domain.UserHistory;
import com.interface21.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql, () -> {
            Parameters parameters = new Parameters();
            parameters.add(1, userHistory.getUserId());
            parameters.add(2, userHistory.getAccount());
            parameters.add(3, userHistory.getPassword());
            parameters.add(4, userHistory.getEmail());
            parameters.add(5, userHistory.getCreatedAt());
            parameters.add(6, userHistory.getCreateBy());
            return parameters;
        });

        log.debug("query : {}", sql);
    }
}
