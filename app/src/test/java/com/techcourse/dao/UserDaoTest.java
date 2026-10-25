package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() throws SQLException {
        final var dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute("truncate table users restart identity");
        }

        userDao = new UserDao(dataSource);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        userDao.insert(new User("other", "other-password", "other@example.com"));

        final var users = userDao.findAll();

        assertThat(users)
                .extracting(User::getId, User::getAccount, User::getPassword, User::getEmail)
                .containsExactlyInAnyOrder(
                        tuple(1L, "gugu", "password", "hkkang@woowahan.com"),
                        tuple(2L, "other", "other-password", "other@example.com"));
    }

    @Test
    void findById() {
        final var user = userDao.findById(1L);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getAccount()).isEqualTo("gugu");
        assertThat(user.getPassword()).isEqualTo("password");
        assertThat(user.getEmail()).isEqualTo("hkkang@woowahan.com");
    }

    @Test
    void findByIdReturnsNullWhenNotFound() {
        assertThat(userDao.findById(999L)).isNull();
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
        assertThat(user.getId()).isEqualTo(1L);
    }

    @Test
    void findByAccountReturnsNullWhenNotFound() {
        assertThat(userDao.findByAccount("unknown")).isNull();
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "insert-password", "insert@example.com");
        userDao.insert(user);

        final var actual = userDao.findById(2L);

        assertThat(actual.getAccount()).isEqualTo(account);
        assertThat(actual.getPassword()).isEqualTo("insert-password");
        assertThat(actual.getEmail()).isEqualTo("insert@example.com");
    }

    @Test
    void update() {
        userDao.insert(new User("other", "other-password", "other@example.com"));
        final var user = new User(1L, "updated-gugu", "password99", "updated@example.com");

        userDao.update(user);

        final var actual = userDao.findById(1L);
        assertThat(actual.getAccount()).isEqualTo("updated-gugu");
        assertThat(actual.getPassword()).isEqualTo("password99");
        assertThat(actual.getEmail()).isEqualTo("updated@example.com");
        assertThat(userDao.findById(2L).getPassword()).isEqualTo("other-password");
    }
}
