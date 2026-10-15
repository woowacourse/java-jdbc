package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
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
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        final var jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        jdbcTemplate.update("truncate table users restart identity");
        userDao = new UserDao(jdbcTemplate);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users)
                .extracting(User::getId, User::getAccount, User::getPassword, User::getEmail)
                .containsExactly(tuple(1L, "gugu", "password", "hkkang@woowahan.com"));
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
    void findAllReturnsEmptyListWhenNoUsersExist() {
        new JdbcTemplate(DataSourceConfig.getInstance()).update("delete from users");

        assertThat(userDao.findAll()).isEmpty();
    }

    @Test
    void findAllReturnsEveryUser() {
        userDao.insert(new User("other", "otherPassword", "other@example.com"));

        assertThat(userDao.findAll()).extracting(User::getAccount).containsExactlyInAnyOrder("gugu", "other");
    }

    @Test
    void missingUserReturnsNull() {
        assertThat(userDao.findById(99L)).isNull();
        assertThat(userDao.findByAccount("missing")).isNull();
    }

    @Test
    void updateChangesOnlyTheRequestedUser() {
        userDao.insert(new User("other", "otherPassword", "other@example.com"));
        final var user = userDao.findById(1L);
        user.changePassword("newPassword");

        userDao.update(user);

        assertThat(userDao.findById(1L)).usingRecursiveComparison().isEqualTo(user);
        assertThat(userDao.findById(2L).getPassword()).isEqualTo("otherPassword");
    }

    @Test
    void accountContainingAQuoteIsStoredAndQueriedAsData() {
        final var account = "o'reilly";
        userDao.insert(new User(account, "password", "other@example.com"));

        assertThat(userDao.findByAccount(account).getAccount()).isEqualTo(account);
        assertThat(userDao.findByAccount("gugu' OR '1'='1")).isNull();
    }

    @Test
    void invalidInsertThrowsDataAccessExceptionWithoutChangingExistingUsers() {
        assertThatThrownBy(() -> userDao.insert(new User("invalid", null, "other@example.com")))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);

        assertThat(userDao.findAll()).extracting(User::getAccount).containsExactly("gugu");
    }

    @Test
    void dataSourceConstructorRemainsUsable() {
        final var dao = new UserDao(DataSourceConfig.getInstance());

        assertThat(dao.findById(1L).getAccount()).isEqualTo("gugu");
    }
}
