package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.SQLIntegrityConstraintViolationException;
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
        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findAllReturnsImmutableList() {
        final var users = userDao.findAll();

        assertThat(users).hasSize(1);
        assertThatThrownBy(() -> users.add(new User("other", "password", "other@example.com")))
                .isInstanceOf(UnsupportedOperationException.class);
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
    void insertRejectsDuplicateAccount() {
        assertThatThrownBy(() -> userDao.insert(new User("gugu", "other-password", "other@example.com")))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLIntegrityConstraintViolationException.class);

        final var user = userDao.findByAccount("gugu");

        assertThat(userDao.findAll()).hasSize(1);
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getPassword()).isEqualTo("password");
    }

    @Test
    void findByIdReturnsNullWhenNotFound() {
        assertThat(userDao.findById(-1L)).isNull();
    }

    @Test
    void findByAccountReturnsNullWhenNotFound() {
        assertThat(userDao.findByAccount("missing-account")).isNull();
    }

    @Test
    void findAllReturnsImmutableEmptyListWhenNotFound() {
        new JdbcTemplate(DataSourceConfig.getInstance()).update("delete from users");

        final var users = userDao.findAll();

        assertThat(users).isEmpty();
        assertThatThrownBy(() -> users.add(new User("other", "password", "other@example.com")))
                .isInstanceOf(UnsupportedOperationException.class);
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
