package com.techcourse.dao;

import com.interface21.dao.IncorrectResultSizeDataAccessException;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() throws SQLException {
        final var dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        try (
                final var connection = dataSource.getConnection();
                final var statement = connection.createStatement()
        ) {
            statement.execute("truncate table users restart identity");
        }

        userDao = new UserDao(dataSource);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
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
    void findByIdThrowsWhenUserDoesNotExist() {
        assertThatThrownBy(() -> userDao.findById(2L))
                .isInstanceOfSatisfying(IncorrectResultSizeDataAccessException.class, exception -> {
                    assertThat(exception.getExpectedSize()).isEqualTo(1);
                    assertThat(exception.getActualSize()).isZero();
                });
    }

    @Test
    void findByAccountThrowsWhenUserDoesNotExist() {
        assertThatThrownBy(() -> userDao.findByAccount("missing"))
                .isInstanceOfSatisfying(IncorrectResultSizeDataAccessException.class, exception -> {
                    assertThat(exception.getExpectedSize()).isEqualTo(1);
                    assertThat(exception.getActualSize()).isZero();
                });
    }

    @Test
    void findByAccountThrowsWhenMultipleUsersHaveTheSameAccount() {
        userDao.insert(new User("gugu", "other-password", "other@example.com"));

        assertThatThrownBy(() -> userDao.findByAccount("gugu"))
                .isInstanceOfSatisfying(IncorrectResultSizeDataAccessException.class, exception -> {
                    assertThat(exception.getExpectedSize()).isEqualTo(1);
                    assertThat(exception.getActualSize()).isEqualTo(2);
                });
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
}
