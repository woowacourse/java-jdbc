package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        new JdbcTemplate(DataSourceConfig.getInstance()).update("drop all objects");
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        userDao.insert(new User("another-user", "another-password", "another@example.com"));
        final var users = userDao.findAll();

        assertThat(users).extracting(User::getAccount)
                .containsExactlyInAnyOrder("gugu", "another-user");
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
    void findByAccountThrowsWhenMultipleUsersHaveTheSameAccount() {
        userDao.insert(new User("gugu", "another-password", "another@example.com"));

        assertThatThrownBy(() -> userDao.findByAccount("gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("Expected at most 1 result, but found 2");
    }

    @Test
    void findByIdReturnsNullWhenUserDoesNotExist() {
        assertThat(userDao.findById(999L)).isNull();
    }

    @Test
    void findByAccountReturnsNullWhenUserDoesNotExist() {
        assertThat(userDao.findByAccount("missing-user")).isNull();
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
    void findByIdWithConnectionReadsUncommittedUpdate() throws SQLException {
        try (final var connection = DataSourceConfig.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                final var user = userDao.findById(connection, 1L);
                user.changePassword("changed-password");

                userDao.update(connection, user);

                assertThat(userDao.findById(connection, 1L).getPassword()).isEqualTo("changed-password");
                assertThat(userDao.findById(1L).getPassword()).isEqualTo("password");
            } finally {
                connection.rollback();
            }

            assertThat(connection.isClosed()).isFalse();
        }

        assertThat(userDao.findById(1L).getPassword()).isEqualTo("password");
    }
}
