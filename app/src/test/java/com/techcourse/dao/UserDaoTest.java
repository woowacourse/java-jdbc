package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserDaoTest {

    private UserDao userDao;
    private Connection connection;

    @BeforeEach
    void setup() throws SQLException {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        final DataSource dataSource = DataSourceConfig.getInstance();
        userDao = new UserDao(dataSource);
        connection = dataSource.getConnection();
        final var user = new User("hongsil", "486", "gurwns9325@gmail.com");
        userDao.insert(connection, user);
    }

    @Test
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final var user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("hongsil");
    }

    @Test
    void findById_make_exception_when_no_result() {
        assertThatThrownBy(() -> userDao.findById(100000000L))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    void findByAccount() {
        final var account = "mylove_hongsil";
        userDao.insert(connection, new User(account, "비밀번호486", "love@with.you"));
        final var user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void findByAccount_make_exception_when_multiple_result() {
        final var user = new User("ditoo", "password", "ditoo@gmail.com");
        userDao.insert(connection, user);
        userDao.insert(connection, user);
        assertThatThrownBy(() -> userDao.findByAccount(user.getAccount()))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(connection, user);

        final var actual = userDao.findById(2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(connection, user);

        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
