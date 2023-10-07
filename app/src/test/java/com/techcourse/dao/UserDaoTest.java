package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class UserDaoTest {

    private UserDao userDao;
    private Connection conn;

    @BeforeEach
    void setup() throws SQLException {
        DataSource dataSource = DataSourceConfig.getInstance();
        conn = dataSource.getConnection();
        DatabasePopulatorUtils.execute(dataSource);

        userDao = new UserDao(DataSourceConfig.getInstance());
        final var user = new User("gitchan", "password", "gitchan@naver.com");
        userDao.insert(conn, user);
    }

    @AfterEach
    void tearDown() {
        userDao.deleteAll(conn);
    }

    @Test
    void findAll() {
        final var users = userDao.findAll();

        assertAll(
                () -> assertThat(users).isNotEmpty(),
                () -> assertThat(users.size()).isEqualTo(1)
        );
    }

    @Test
    void findById() {
        final var findUser = userDao.findById(1L).get();

        assertAll(
                () -> assertThat(findUser.getAccount()).isEqualTo("gitchan"),
                () -> assertThat(findUser.getPassword()).isEqualTo("password"),
                () -> assertThat(findUser.getEmail()).isEqualTo("gitchan@naver.com")
        );
    }

    @Test
    void findByAccount() {
        final String account = "gitchan";
        final User findUser = userDao.findByAccount(account).get();

        assertAll(
                () -> assertThat(findUser.getAccount()).isEqualTo("gitchan"),
                () -> assertThat(findUser.getPassword()).isEqualTo("password"),
                () -> assertThat(findUser.getEmail()).isEqualTo("gitchan@naver.com")
        );
    }

    @Test
    void insert() {
        final var account = "insert-gitchan";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(conn, user);

        final var actual = userDao.findById(2L).get();

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L).get();
        user.changePassword(newPassword);

        userDao.update(conn, user);

        final var actual = userDao.findById(1L).get();

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
