package com.techcourse.dao;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserHistoryDaoTest {

    private static final UserHistory USER_HISTORY = new UserHistory(1L,
            1L,
            "joy",
            "joy1234",
            "joy@gmail.com",
            "");

    private final DataSource dataSource = DataSourceConfig.getInstance();

    private UserHistoryDao userHistoryDao;

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.execute(dataSource);
        userHistoryDao = new UserHistoryDao(dataSource);
    }

    @Test
    void log() {
        assertDoesNotThrow(
                () -> userHistoryDao.log(USER_HISTORY)
        );
    }
}
