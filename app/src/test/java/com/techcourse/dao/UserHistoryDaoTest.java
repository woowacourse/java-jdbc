package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserHistoryDaoTest {

    private UserHistoryDao userHistoryDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userHistoryDao = new UserHistoryDao(DataSourceConfig.getInstance());
        UserHistory userHistory = new UserHistory(1L, 1L, "gugu", "password", "hkkang@woowahan.com", "pedro");
        userHistoryDao.log(userHistory);
    }

    @DisplayName("UserHistory를 조회한다.")
    @Test
    void findById() {
        // when
        UserHistory userHistory = userHistoryDao.findById(1L);

        // then
        assertAll(
                () -> assertThat(userHistory.getUserId()).isEqualTo(1L),
                () -> assertThat(userHistory.getAccount()).isEqualTo("gugu"),
                () -> assertThat(userHistory.getPassword()).isEqualTo("password"),
                () -> assertThat(userHistory.getEmail()).isEqualTo("hkkang@woowahan.com"),
                () -> assertThat(userHistory.getCreateBy()).isEqualTo("pedro")
        );
    }

    @DisplayName("UserHistory를 로그로 남긴다.")
    @Test
    void insert() {
        // given
        UserHistory userHistory = new UserHistory(2L, 2L, "pedro", "password", "pedro@example.org", "gugu");

        // when
        userHistoryDao.log(userHistory);

        // then
        UserHistory actual = userHistoryDao.findById(2L);
        assertAll(
                () -> assertThat(actual.getUserId()).isEqualTo(userHistory.getUserId()),
                () -> assertThat(actual.getAccount()).isEqualTo(userHistory.getAccount()),
                () -> assertThat(actual.getPassword()).isEqualTo(userHistory.getPassword()),
                () -> assertThat(actual.getEmail()).isEqualTo(userHistory.getEmail()),
                () -> assertThat(actual.getCreateBy()).isEqualTo(userHistory.getCreateBy())
        );
    }

}
