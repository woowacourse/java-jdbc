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
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userDao = new UserDao(jdbcTemplate);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("TRUNCATE TABLE users");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    void findAll() {
        // given
        userDao.insert(new User("user1", "pass1", "user1@test.com"));
        userDao.insert(new User("user2", "pass2", "user2@test.com"));

        // when
        var users = userDao.findAll();

        // then
        assertThat(users).hasSize(2);
    }

    @Test
    void findById() {
        // given
        var user = new User("gugu", "password", "gugu@test.com");
        userDao.insert(user);

        // when
        var found = userDao.findById(1L);

        // then
        assertThat(found.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        // given
        var account = "gugu";
        var user = new User(account, "password", "gugu@test.com");
        userDao.insert(user);

        // when
        var found = userDao.findByAccount(account);

        // then
        assertThat(found.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        // given
        var user = new User("insert-gugu", "password", "hkkang@woowahan.com");

        // when
        userDao.insert(user);

        // then
        var found = userDao.findById(1L);
        assertThat(found.getEmail()).isEqualTo("hkkang@woowahan.com");
    }

    @Test
    void update() {
        // given
        var user = new User("gugu", "oldpass", "gugu@test.com");
        userDao.insert(user);
        var saved = userDao.findByAccount("gugu");

        // when
        saved.changePassword("newpass");
        userDao.update(saved);

        // then
        var updated = userDao.findByAccount("gugu");
        assertThat(updated.getPassword()).isEqualTo("newpass");
    }
}
