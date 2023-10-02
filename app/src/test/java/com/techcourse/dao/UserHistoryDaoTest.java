package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UserHistoryDaoTest {

    private UserHistoryDao userHistoryDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userHistoryDao = new UserHistoryDao(DataSourceConfig.getInstance());
    }

    @Test
    void log() {
        // given
        final String createdBy = "ocean";
        final User user = new User(1L, "gugu", "password", "hkkang@woowahan.com");
        final UserHistory userHistory = new UserHistory(user, createdBy);

        // when
        userHistoryDao.log(userHistory);

        // then
        final UserHistory actual = userHistoryDao.findLogByUser(user);
        assertThat(actual.getCreateBy()).isEqualTo(createdBy);
    }
}
