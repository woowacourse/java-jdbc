package com.techcourse.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserHistoryDaoTest {

    private UserHistoryDao userHistoryDao;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        jdbcTemplate.update("DROP TABLE IF EXISTS users");
        userHistoryDao = new UserHistoryDao(jdbcTemplate);
    }

    @DisplayName("UserHistory를 기록한다.")
    @Test
    void log() {
        UserHistory userHistory =
                new UserHistory(1L, 1L, "daon", "daon1", "daon@wooteco.com", "liv");

        userHistoryDao.log(userHistory);

        verify(jdbcTemplate).update(
                eq("insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)"),
                eq(userHistory.getUserId()),
                eq(userHistory.getAccount()),
                eq(userHistory.getPassword()),
                eq(userHistory.getEmail()),
                eq(userHistory.getCreatedAt()),
                eq(userHistory.getCreateBy())
        );
    }
}
