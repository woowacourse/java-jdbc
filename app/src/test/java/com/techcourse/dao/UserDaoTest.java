package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() throws SQLException {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        clearUsers();

        userDao = new UserDao(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findAllWithoutUsers() throws SQLException {
        clearUsers();

        assertThat(userDao.findAll()).isEmpty();
    }

    @Test
    void findAllWithMultipleUsers() {
        userDao.insert(new User("pobi", "another-password", "pobi@example.com"));

        assertThat(userDao.findAll())
                .extracting(User::getId, User::getAccount, User::getPassword, User::getEmail)
                .containsExactlyInAnyOrder(
                        tuple(1L, "gugu", "password", "hkkang@woowahan.com"),
                        tuple(2L, "pobi", "another-password", "pobi@example.com"));
    }

    @Test
    void findById() {
        final var user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByIdWithoutMatchingUser() {
        assertThatThrownBy(() -> userDao.findById(999L))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("단건 조회 결과가 없습니다");
    }

    @Test
    void findByIdReturnsAllFields() {
        final var user = userDao.findById(1L);

        assertThat(user)
                .extracting(User::getId, User::getAccount, User::getPassword, User::getEmail)
                .containsExactly(1L, "gugu", "password", "hkkang@woowahan.com");
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void findByAccountWithoutMatchingUser() {
        assertThatThrownBy(() -> userDao.findByAccount("unknown"))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("단건 조회 결과가 없습니다");
    }

    @Test
    void findByAccountWithDuplicateAccountsThrowsException() {
        userDao.insert(new User("gugu", "another-password", "another@example.com"));

        assertThatThrownBy(() -> userDao.findByAccount("gugu"))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("단건 조회 결과가 여러 건입니다");
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

    private void clearUsers() throws SQLException {
        try (final var connection = DataSourceConfig.getInstance().getConnection();
             final var statement = connection.createStatement()) {
            statement.executeUpdate("truncate table users restart identity");
        }
    }
}
