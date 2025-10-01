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
    void clean(){
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    void findAll() {
        // given
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);

        // when
        final var results = userDao.findAll();

        // then
        assertThat(results).isNotEmpty();
    }

    @Test
    void findById() {
        // given
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);

        // when
        final var result = userDao.findById(1L);

        // then
        assertThat(result.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        // given
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);

        // when
        final var account = "gugu";
        final var result = userDao.findByAccount(account);

        // then
        assertThat(result.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        // when
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        // then
        final var actual = userDao.findById(1L);
        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        // given
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);

        // when
        final var newPassword = "password99";
        final var targetUser = userDao.findById(1L);
        targetUser.changePassword(newPassword);
        userDao.update(targetUser);

        // then
        final var actual = userDao.findById(1L);
        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
