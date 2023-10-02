package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class UserDaoTest {

    private UserDao userDao;
    private User testUser;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userDao = new UserDao(jdbcTemplate);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
        testUser = userDao.findByAccount("gugu");
    }

    @AfterEach
    void clear() {
        final var jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void findAll() {
        userDao.insert(new User("gugu2", "password2", "asd@gmail.com"));
        userDao.insert(new User("gugu3", "password3", "asd@gmail.com"));
        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
        assertThat(users).hasSize(3);
    }

    @Test
    void findById() {
        final var user = userDao.findById(testUser.getId());

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByIdShouldBeNull() {
        final var nullableUser = userDao.findById(100L);

        assertThat(nullableUser).isNull();
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        // given
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        // when
        final var actual = userDao.findByAccount("insert-gugu");

        // then
        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findByAccount("gugu");
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findByAccount("gugu");

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
