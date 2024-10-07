package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcUserHistoryDaoTest {

    private UserHistoryDao userHistoryDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        userHistoryDao = new JdbcUserHistoryDao(DataSourceConfig.getInstance());
    }

    @DisplayName("log 메서드를 실행하면 아무런 예외가 발생하지 않는다.")
    @Test
    void log() {
        UserHistory userHistory = new UserHistory(new User(1L, "reviewer", "atto", "test@test.com"), "daon");

        assertThatCode(() -> userHistoryDao.log(userHistory))
                .doesNotThrowAnyException();
    }
}
