package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import nextstep.jdbc.JdbcTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(new JdbcTemplate(DataSourceConfig.getInstance()));
        var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        var user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        var account = "gugu";
        var user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        var account = "insert-gugu";
        String password = "password";
        String email = "hkkang@woowahan.com";
        var user = new User(account, password, email);
        userDao.insert(user);

        var actual = userDao.findById(2L);

        assertAll(
                () -> assertThat(actual.getAccount()).isEqualTo(account),
                () -> assertThat(actual.getPassword()).isEqualTo(password),
                () -> assertThat(actual.getEmail()).isEqualTo(email)
        );
    }

    @Test
    void update() {
        var newPassword = "password99";
        var user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        var actual = userDao.findById(1L);

        assertAll(
                () -> assertThat(actual.getPassword()).isEqualTo(newPassword),
                () -> assertThat(actual.getAccount()).isEqualTo("gugu"),
                () -> assertThat(actual.getEmail()).isEqualTo("hkkang@woowahan.com")
        );
    }

    @AfterEach
    void truncate() {
        String truncateQuery = "truncate table users";
        String alterColumnQuery  = "ALTER TABLE users AUTO_INCREMENT = 1";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        jdbcTemplate.update(truncateQuery);
        jdbcTemplate.update(alterColumnQuery);
    }
}
