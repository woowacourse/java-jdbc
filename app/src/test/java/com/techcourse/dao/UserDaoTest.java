package com.techcourse.dao;

import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserDaoTest {

    private JdbcDataSource dataSource;
    private UserDao userDao;

    @BeforeEach
    void setup() {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:dao-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        DatabasePopulatorUtils.execute(dataSource);

        userDao = new UserDao(dataSource);
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
    }

    @Test
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
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
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void updateWithConnection() throws SQLException {
        final var newPassword = "passwordWithConnection";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);

        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            userDao.update(connection, user);

            assertThat(connection.isClosed()).isFalse();
            connection.commit();
        }

        assertThat(userDao.findById(1L).getPassword()).isEqualTo(newPassword);
    }

    @Test
    void findByAccountNotFound() {
        assertThat(userDao.findByAccount("unknown")).isNull();
    }

    @Test
    void updateNonExistentUser() {
        final var user = new User(
                999L,
                "unknown",
                "newPassword",
                "unknown@example.com"
        );

        assertThatThrownBy(() -> userDao.update(user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("999");
    }
}
