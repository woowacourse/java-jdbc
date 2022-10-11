package com.techcourse.dao;

import java.sql.Connection;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.techcourse.domain.UserHistory;

import nextstep.jdbc.JdbcTemplate;

public class JdbcUserHistoryDao implements UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(JdbcUserHistoryDao.class);

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserHistoryDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public JdbcUserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void log(final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, userHistory.getUserId(), userHistory.getAccount(), userHistory.getPassword()
            , userHistory.getEmail(), userHistory.getCreatedAt(), userHistory.getCreateBy());
    }

    @Override
    public void log(final Connection connection, final UserHistory userHistory) {
        final var sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, connection, userHistory.getUserId(), userHistory.getAccount(),
            userHistory.getPassword()
            , userHistory.getEmail(), userHistory.getCreatedAt(), userHistory.getCreateBy());
    }
}
