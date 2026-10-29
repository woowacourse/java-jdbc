package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        new JdbcTemplate(DataSourceConfig.getInstance()).update("truncate table users restart identity");

        userDao = new UserDao(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        userDao.insert(new User("second-user", "password2", "second@example.com"));

        final var users = userDao.findAll();

        assertThat(users).extracting(User::getAccount)
                .containsExactlyInAnyOrder("gugu", "second-user");
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
    void missingUserReturnsNull() {
        assertThat(userDao.findById(999L)).isNull();
        assertThat(userDao.findByAccount("missing-account")).isNull();
    }

    @Test
    void duplicateAccountThrowsException() {
        userDao.insert(new User("gugu", "another-password", "another@example.com"));

        assertThatThrownBy(() -> userDao.findByAccount("gugu"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Expected at most one row, but found more than one");
    }

    @Test
    void findWithInjectedJdbcTemplate() {
        final var dao = new UserDao(new JdbcTemplate(DataSourceConfig.getInstance()));

        assertThat(dao.findById(1L).getAccount()).isEqualTo("gugu");
        assertThat(dao.findByAccount("gugu").getId()).isEqualTo(1L);
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
