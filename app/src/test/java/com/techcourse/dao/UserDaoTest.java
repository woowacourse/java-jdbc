package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        userDao = new UserDao(DataSourceConfig.getInstance());
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("TRUNCATE TABLE users");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    void findAll() {
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var foundUser = userDao.findById(1L);

        assertThat(foundUser.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var account = "gugu";
        final var foundUser = userDao.findByAccount(account);

        assertThat(foundUser.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");

        userDao.insert(user);

        final var actual = userDao.findById(1L);
        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
        final var foundUser = userDao.findById(1L);

        final var newPassword = "password99";
        foundUser.changePassword(newPassword);
        userDao.update(foundUser);

        final var actual = userDao.findById(1L);
        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
