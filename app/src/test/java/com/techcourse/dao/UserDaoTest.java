package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import javax.sql.DataSource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        DataSource dataSource = DataSourceConfig.getInstance();
        jdbcTemplate = new JdbcTemplate(dataSource);
        DatabasePopulatorUtils.execute(dataSource);

        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");

        userDao = new UserDao(jdbcTemplate);
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
        final var optionalUser = userDao.findById(1L);

        Assertions.assertThat(optionalUser)
                .get()
                .extracting(User::getAccount)
                .isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var optionalUser = userDao.findByAccount(account);

        Assertions.assertThat(optionalUser)
                .get()
                .extracting(User::getAccount)
                .isEqualTo(account);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var optionalActual = userDao.findById(2L);

        Assertions.assertThat(optionalActual)
                .get()
                .extracting(User::getAccount)
                .isEqualTo(account);
    }

    @Test
    void update() {
         // given
        final var newPassword = "password99";
        final var user = userDao.findById(1L).get();
        user.changePassword(newPassword);

        // when
        userDao.update(user);

        // then
        final var optionalActual = userDao.findById(1L);

        Assertions.assertThat(optionalActual)
                .get()
                .extracting(User::getPassword)
                .isEqualTo(newPassword);
    }
}
