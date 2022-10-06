package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.UserHistory;
import nextstep.jdbc.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserHistoryDaoTest {

    private UserHistoryDao userHistoryDao;

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userHistoryDao = new UserHistoryDao(DataSourceConfig.getInstance());
    }

    @Test
    void test() {
        UserHistory userHistory = new UserHistory(1L, 1L, "asd", "asd", "asd", "asd");
        userHistoryDao.log(userHistory);
    }
}
