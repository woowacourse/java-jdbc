package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserHistoryDaoTest {

    private UserHistoryDao userHistoryDao;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource datasource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(datasource);

        datasource.getConnection();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(datasource);
        userHistoryDao = new UserHistoryDao(jdbcTemplate);

        setDefaultData();
    }

    private void setDefaultData() {
        User user = new User(1L, "jazz", "1130", "jazz@woowahan.com");
        UserHistory userHistory = new UserHistory(user, "2024-10-03T01:56:00");
        userHistoryDao.log(userHistory);
    }

    @DisplayName("UserHistory를 저장한다.")
    @Test
    void insert() {
        UserHistory userHistory = new UserHistory(
                2L,
                2L,
                "gugu",
                "password",
                "hkkang@woowahan.com",
                "2024-10-03T01:56:00"
        );
        userHistoryDao.log(userHistory);

        UserHistory actual = userHistoryDao.findById(2L);

        assertAll(
                () -> assertThat(actual.getUserId()).isEqualTo(userHistory.getUserId()),
                () -> assertThat(actual.getAccount()).isEqualTo(userHistory.getAccount()),
                () -> assertThat(actual.getPassword()).isEqualTo(userHistory.getPassword()),
                () -> assertThat(actual.getEmail()).isEqualTo(userHistory.getEmail()),
                () -> assertThat(actual.getCreateBy()).isEqualTo(userHistory.getCreateBy())
        );
    }

    @DisplayName("UserHistory를 조회한다.")
    @Test
    void findById() {
        UserHistory userHistory = userHistoryDao.findById(1L);

        assertAll(
                () -> assertThat(userHistory.getUserId()).isEqualTo(1L),
                () -> assertThat(userHistory.getAccount()).isEqualTo("jazz"),
                () -> assertThat(userHistory.getPassword()).isEqualTo("1130"),
                () -> assertThat(userHistory.getEmail()).isEqualTo("jazz@woowahan.com"),
                () -> assertThat(userHistory.getCreateBy()).isEqualTo("2024-10-03T01:56:00")
        );
    }
}
