package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        if (userDao.findAll().isEmpty()) {
            userDao.insert(user);
        }
    }

    @Test
    void findAll() {
        final var newbie = new User("gugu2", "password2", "hkkang2@woowahan.com");
        userDao.insert(newbie);

        final var users = userDao.findAll();

        List<String> actual = users.stream()
                .map(User::getAccount)
                .toList();
        List<String> expected = List.of("gugu", "gugu2");
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

        userDao.delete(newbie);
    }

    @Test
    void findById() {
        final var user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(2L);

        assertThat(actual.getAccount()).isEqualTo(account);

        userDao.delete(user);
    }

    @Test
    void update() throws SQLException {
        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        Connection connection = DataSourceConfig.getInstance().getConnection();
        user.changePassword(newPassword);

        userDao.update(user,connection);

        final var actual = userDao.findById(1L);
        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
