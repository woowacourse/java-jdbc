package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class UserDaoTest {

    private UserDao userDao;
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(TestDataSourceConfig.getInstance());

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(TestDataSourceConfig.getInstance());
        userDao = new UserDao(TestDataSourceConfig.getInstance());
        jdbcTemplate.update("TRUNCATE TABLE users RESTART IDENTITY;");
        jdbcTemplate.update("TRUNCATE TABLE user_history RESTART IDENTITY;");

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
        final var user = userDao.findById(1L).get();

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account).get();
        List<User> userList = userDao.findAll();

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang2@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(2L).get();

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L).get();
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L).get();

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
