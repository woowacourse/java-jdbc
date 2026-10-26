package com.techcourse.dao;

import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
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
        assertThat(userDao.findAll()).isNotEmpty();
    }

    @Test
    void findById() {
        final var user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final var user = userDao.findByAccount("gugu");

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void insert() {
        final var user = new User(
                "insert-gugu",
                "password",
                "hkkang@woowahan.com"
        );

        userDao.insert(user);

        assertThat(userDao.findById(2L).getAccount())
                .isEqualTo("insert-gugu");
    }

    @Test
    void update() {
        final var user = userDao.findById(1L);
        user.changePassword("password99");

        userDao.update(user);

        assertThat(userDao.findById(1L).getPassword())
                .isEqualTo("password99");
    }

    @Test
    void updateWithinTransaction() throws SQLException {
        final var user = userDao.findById(1L);
        user.changePassword("newPassword");

        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            TransactionSynchronizationManager.bindResource(
                    dataSource, connection
            );

            try {
                userDao.update(user);

                assertThat(connection.isClosed()).isFalse();
                assertThat(userDao.findById(1L).getPassword())
                        .isEqualTo("newPassword");

                connection.rollback();
            } finally {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        }

        assertThat(userDao.findById(1L).getPassword())
                .isEqualTo("password");
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
