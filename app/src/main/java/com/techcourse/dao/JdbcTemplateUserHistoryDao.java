package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;

public class JdbcTemplateUserHistoryDao implements UserHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTemplateUserHistoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void log(UserHistory userHistory) {

    }
}
