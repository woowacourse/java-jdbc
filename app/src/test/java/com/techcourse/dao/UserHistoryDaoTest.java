package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class UserHistoryDaoTest {

    private UserHistoryDao userHistoryDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userHistoryDao = new UserHistoryDao(DataSourceConfig.getInstance());
    }

    @DisplayName("UserHistory를 저장한다.")
    @Test
    void log() {
        // given
        final var user = new User(1L, "gugu", "password", "hkkang@woowahan.com");
        final UserHistory userHistory = new UserHistory(user, "gugu");

        // when & then
        assertDoesNotThrow(() -> userHistoryDao.log(userHistory));
    }
}
