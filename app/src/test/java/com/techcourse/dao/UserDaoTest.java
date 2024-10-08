package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");

        userDao = new UserDao(jdbcTemplate);
        final var user1 = new User("gugu", "password", "hkkang@woowahan.com");
        final var user2 = new User("wonny", "1q2w3e4r", "wonny@woowahan.com");
        userDao.insert(user1);
        userDao.insert(user2);
    }

    @Test
    @DisplayName("Find all users from the datasource.")
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users)
                .hasSize(2)
                .isNotEmpty();
    }

    @Test
    @DisplayName("Find a user by id from the datasource.")
    void findById() {
        final var user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    @DisplayName("Find a user by account from the datasource.")
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    @DisplayName("Insert a user into the datasource.")
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(3L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    @DisplayName("Update password of a user from the datasource.")
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
