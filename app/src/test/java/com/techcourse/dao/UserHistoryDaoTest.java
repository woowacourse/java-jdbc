package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserHistoryDaoTest {

    private static final DataSource DATA_SOURCE = DataSourceConfig.getInstance();
    private UserHistoryDao userHistoryDao;

    @BeforeEach
    void setup() throws SQLException {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        userHistoryDao = new UserHistoryDao(DATA_SOURCE);

        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        final var userHistory = new UserHistory(
                1L,
                1L,
                user.getAccount(),
                user.getPassword(),
                user.getEmail(),
                "tester"
        );

        userHistoryDao.log(DATA_SOURCE.getConnection(), userHistory);
    }

    @Test
    void findAll() {
        final var userHistories = userHistoryDao.findAll();

        assertThat(userHistories).isNotEmpty();
    }

    @Test
    void findById() {
        final var userHistory = userHistoryDao.findById(1L);

        assertThat(userHistory.getAccount()).isEqualTo("gugu");
    }

    @Test
    void insert() throws SQLException {
        final var account = "insert-gugu";
        final var newUser = new User(account, "password", "newemail@woowahan.com");
        final var newUserHistory = new UserHistory(
                null,
                2L,
                newUser.getAccount(),
                newUser.getPassword(),
                newUser.getEmail(),
                "tester"
        );

        userHistoryDao.log(DATA_SOURCE.getConnection(), newUserHistory);

        final var actual = userHistoryDao.findById(2L);
        assertThat(actual.getAccount()).isEqualTo(account);
    }
}

