package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserHistoryDaoTest {

    private UserHistoryDao userHistoryDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userHistoryDao = new UserHistoryDao(DataSourceConfig.getInstance());
    }

    @Test
    void log() {
        UserHistory userHistory = new UserHistory(
            1L,
            1L,
            "account",
            "password",
            "email",
            "pooh"
        );
        userHistoryDao.log(userHistory);

        UserHistory savedHistory = userHistoryDao.findById(1L);

        assertThat(userHistory).usingRecursiveComparison().isEqualTo(savedHistory);
    }
}
