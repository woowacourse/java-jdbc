package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(DataSourceConfig.getInstance());
        new com.interface21.jdbc.core.JdbcTemplate(DataSourceConfig.getInstance())
                .update("truncate table users restart identity");
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
    void missingUser() {
        assertThat(userDao.findById(-1L)).isNull();
        assertThat(userDao.findByAccount("missing")).isNull();
    }

    @Test
    void quotedAccountIsBoundAsData() {
        final var account = "o'reilly' OR '1'='1";
        userDao.insert(new User(account, "password", "test@example.com"));
        assertThat(userDao.findByAccount(account).getAccount()).isEqualTo(account);
        assertThat(userDao.findAll()).hasSize(2);
    }

    @Test
    void templateConstructor() {
        final var dao = new UserDao(new com.interface21.jdbc.core.JdbcTemplate(DataSourceConfig.getInstance()));
        assertThat(dao.findByAccount("gugu").getEmail()).isEqualTo("hkkang@woowahan.com");
    }
}
