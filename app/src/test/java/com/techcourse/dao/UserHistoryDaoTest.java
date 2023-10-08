package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThatNoException;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class UserHistoryDaoTest {

    private UserHistoryDao userHistoryDao;

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userHistoryDao = new UserHistoryDao(jdbcTemplate);
    }

    @Test
    void 기록을_남긴다() {
        // given
        UserHistory userHistory = new UserHistory(new User(1L, "huchu", "password", "huchu@woowa.com"), "now");

        // expect
        assertThatNoException().isThrownBy(() -> userHistoryDao.log(userHistory));
    }
}
