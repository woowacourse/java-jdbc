package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;

class UserHistoryDaoTest {

    private final UserHistoryDao userHistoryDao = new UserHistoryDao(new JdbcTemplate(DataSourceConfig.getInstance()));

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
    }

    @Test
    void insert() {
        UserHistory userHistory = new UserHistory(1L, 1L, "eden", "povverGuy", "eden@wtc.com", "2024-10-12");
        userHistoryDao.log(userHistory);

        var actual = userHistoryDao.findById(1L);

        assertAll(
                () -> assertThat(actual.getAccount()).isEqualTo(userHistory.getAccount()),
                () -> assertThat(actual.getId()).isEqualTo(userHistory.getId())
        );
    }

}
