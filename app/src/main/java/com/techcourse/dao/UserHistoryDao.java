package com.techcourse.dao;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.SqlParameterSource;
import com.techcourse.domain.UserHistory;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(final UserHistory userHistory) {
        final String baseQuery = "INSERT INTO user_history (user_id, account, password, email, created_at, created_by) "
                + "VALUES (:userId, :account, :password, :email, :createdAt, :createdBy)";
        final SqlParameterSource sqlParameterSource = new SqlParameterSource(userHistory);
        jdbcTemplate.insert(baseQuery, sqlParameterSource);
    }
}
