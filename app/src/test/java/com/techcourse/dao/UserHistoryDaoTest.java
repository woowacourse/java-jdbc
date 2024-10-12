package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;

class UserHistoryDaoTest {

    private UserHistoryDao userHistoryDao;

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        userHistoryDao = new UserHistoryDao(DataSourceConfig.getInstance());
    }

    @Test
    void log() throws SQLException {
        // given
        final UserHistory userHistory = new UserHistory(1L, 13L, "account", "password", "email@mail.e", "createdBy");

        // when
        userHistoryDao.log(userHistory);

        // then
        assertThat(userHistoryDao.findById(userHistory.getId())).isEqualTo(userHistory);
    }
}
