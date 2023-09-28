package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThatCode;

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
        this.userHistoryDao = new UserHistoryDao(DataSourceConfig.getInstance());
    }

    @Test
    void savingUserHistory() {
        assertThatCode(() -> userHistoryDao.log(new UserHistory(1L, 1L, "account", "password", "email", "now")))
                .doesNotThrowAnyException();
    }
}
