package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.Test;

class UserHistoryDaoTest {

    private UserHistoryDao userHistoryDao;

    @Test
    void log() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userHistoryDao = new UserHistoryDao(DataSourceConfig.getInstance());
        final User user = new User(1L, "irene", "password", "irene@irene.com");
        final UserHistory userHistory = new UserHistory(user, "irene");

        assertThatCode(() -> userHistoryDao.log(userHistory)).doesNotThrowAnyException();
    }
}
