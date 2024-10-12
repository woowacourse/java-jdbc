package com.techcourse.dao;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;
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
        Connection connection = mock(Connection.class);
        UserHistory userHistory =
                new UserHistory(1L, 1L, "daon", "daon1", "daon@wooteco.com", "liv");

        userHistoryDao.log(connection, userHistory);

        verify(jdbcTemplate).update(
                connection,
                "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)",
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy()
        );
    }
}
