package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.interface21.jdbc.core.JdbcTemplate;
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
        new JdbcTemplate(DataSourceConfig.getInstance())
                .update("truncate table users restart identity", new Object[0]);

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
    void findAllMapsEveryRow() {
        userDao.insert(new User("pino", "other-password", "pino@example.com"));

        final var users = userDao.findAll();

        assertThat(users).extracting(User::getAccount).containsExactlyInAnyOrder("gugu", "pino");
        final var pino = userDao.findByAccount("pino");
        assertThat(pino.getId()).isEqualTo(2L);
        assertThat(pino.getPassword()).isEqualTo("other-password");
        assertThat(pino.getEmail()).isEqualTo("pino@example.com");
    }

    @Test
    void missingUserThrowsException() {
        assertThatThrownBy(() -> userDao.findById(999L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Expected 1 row but found 0");
        assertThatThrownBy(() -> userDao.findByAccount("missing"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Expected 1 row but found 0");
    }

    @Test
    void duplicateAccountThrowsException() {
        userDao.insert(new User("gugu", "other-password", "other@example.com"));

        assertThatThrownBy(() -> userDao.findByAccount("gugu"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Expected 1 row but found 2");
    }

    @Test
    void findAllReturnsEmptyListWhenThereAreNoUsers() {
        new JdbcTemplate(DataSourceConfig.getInstance()).update("delete from users", new Object[0]);

        assertThat(userDao.findAll()).isEmpty();
    }

    @Test
    void acceptsJdbcTemplate() {
        final var dao = new UserDao(new JdbcTemplate(DataSourceConfig.getInstance()));

        assertThat(dao.findById(1L).getAccount()).isEqualTo("gugu");
    }
}
