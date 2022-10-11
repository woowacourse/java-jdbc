package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.UserHistory;
import java.sql.SQLException;
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
    void test() throws SQLException {
        UserHistory userHistory = new UserHistory(1L, 1L, "asd", "asd", "asd", "asd");
        userHistoryDao.log(DataSourceConfig.getInstance().getConnection(), userHistory);
    }
}
