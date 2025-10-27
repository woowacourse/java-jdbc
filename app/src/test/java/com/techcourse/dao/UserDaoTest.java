package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private UserDao userDao;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userDao = new UserDao(DataSourceConfig.getInstance());

        // 테스트 데이터 초기화 - 기존 데이터 모두 삭제
        jdbcTemplate.update("DELETE FROM USERS");
    }

    @Test
    void findAll() {
        // given
        userDao.insert(new User("gugu", "password", "gugu@email.com"));
        userDao.insert(new User("pobi", "password", "pobi@email.com"));

        // when
        final var users = userDao.findAll();

        // then
        assertThat(users).hasSize(2);
    }

    @Test
    void findById() {
        // given
        final var account = "gugu";
        userDao.insert(new User(account, "password", "gugu@email.com"));
        final var savedUser = userDao.findByAccount(account);

        // when
        final var foundUser = userDao.findById(savedUser.getId());

        // then
        assertThat(foundUser.getAccount()).isEqualTo(account);
        assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
    }

    @Test
    void findByAccount() {
        // given
        final var account = "gugu";
        final var email = "gugu@email.com";
        userDao.insert(new User(account, "password", email));

        // when
        final var foundUser = userDao.findByAccount(account);

        // then
        assertThat(foundUser.getAccount()).isEqualTo(account);
        assertThat(foundUser.getEmail()).isEqualTo(email);
    }

    @Test
    void insert() {
        // given
        final var account = "new-user";
        final var email = "new@email.com";
        final var user = new User(account, "password", email);

        // when
        userDao.insert(user);

        // then
        final var foundUser = userDao.findByAccount(account);
        assertThat(foundUser.getAccount()).isEqualTo(account);
        assertThat(foundUser.getEmail()).isEqualTo(email);
    }

    @Test
    void update() {
        // given
        final var account = "gugu";
        final var originalPassword = "password";
        final var newPassword = "newPassword99";

        userDao.insert(new User(account, originalPassword, "gugu@email.com"));
        final var savedUser = userDao.findByAccount(account);

        // when
        savedUser.changePassword(newPassword);
        userDao.update(savedUser);

        // then
        final var updatedUser = userDao.findById(savedUser.getId());
        assertThat(updatedUser.getPassword()).isEqualTo(newPassword);
    }
}
