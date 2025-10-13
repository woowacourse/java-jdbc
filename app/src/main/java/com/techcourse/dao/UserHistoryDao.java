package com.techcourse.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.jpa.SimpleJpa;
import com.techcourse.domain.UserHistory;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJpa simpleJpa;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJpa = new SimpleJpa(jdbcTemplate, "com.techcourse");
    }

    public void log(final UserHistory userHistory) {
        simpleJpa.insert(userHistory);
    }
}
