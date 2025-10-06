package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
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

        try {
            jdbcTemplate.executeUpdate("DELETE FROM users");
        } catch (Exception e) {
        }
    }

    @Test
    void findAll() {
        // given
        userDao.insert(new User("user1", "password1", "user1@example.com"));
        userDao.insert(new User("user2", "password2", "user2@example.com"));

        // when
        final var users = userDao.findAll();

        // then
        assertThat(users).hasSize(2);
        assertThat(users).extracting("account").containsExactlyInAnyOrder("user1", "user2");
        assertThat(users).extracting("email").containsExactlyInAnyOrder("user1@example.com", "user2@example.com");
    }

    @Test
    void findById() {
        // given
        final var testUser = new User("testuser", "password", "test@example.com");
        userDao.insert(testUser);

        // given
        final var foundUser = userDao.findByAccount("testuser").get();

        // when
        final var user = userDao.findById(foundUser.getId()).get();

        // then
        assertThat(user.getAccount()).isEqualTo("testuser");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findByAccount() {
        // given
        final var account = "testaccount";
        final var testUser = new User(account, "password", "test@example.com");
        userDao.insert(testUser);

        // when
        final var user = userDao.findByAccount(account).get();

        // then
        assertThat(user.getAccount()).isEqualTo(account);
        assertThat(user.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void insert() {
        // given
        final var account = "newuser";
        final var user = new User(account, "password", "newuser@example.com");

        // when
        userDao.insert(user);

        // then
        final var actual = userDao.findByAccount(account).get();
        assertThat(actual.getAccount()).isEqualTo(account);
        assertThat(actual.getEmail()).isEqualTo("newuser@example.com");
        assertThat(actual.getPassword()).isEqualTo("password");
    }

    @Test
    void update() {
        // given
        final var testUser = new User("updateuser", "oldpassword", "update@example.com");
        userDao.insert(testUser);
        final var foundUser = userDao.findByAccount("updateuser").get();
        final var newPassword = "newpassword";
        foundUser.changePassword(newPassword);

        // when
        userDao.update(foundUser);

        // then
        final var actual = userDao.findByAccount("updateuser").get();
        assertThat(actual.getPassword()).isEqualTo(newPassword);
        assertThat(actual.getAccount()).isEqualTo("updateuser");
    }
}
