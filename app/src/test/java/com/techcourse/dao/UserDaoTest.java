package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class UserDaoTest {

    private UserDao userDao;
    private JdbcTemplate jdbcTemplate;
    private User user;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        jdbcTemplate.update("delete from users");
        userDao = new UserDao(jdbcTemplate);
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
        user = userDao.findByAccount("gugu").orElseThrow();
    }

    @Test
    void findAll() {
        // when
        final List<User> users = userDao.findAll();

        // then
        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        // when
        final Optional<User> findUser = userDao.findById(user.getId());

        // then
        assertThat(findUser).isPresent();
    }

    @Test
    void findByAccount() {
        // when
        final Optional<User> findUesr = userDao.findByAccount(user.getAccount());

        // then
        assertThat(findUesr).isPresent();
    }

    @Test
    void insert() {
        // given
        final String account = "insert-gugu";
        final User user = new User(account, "password", "hkkang@woowahan.com");

        // when
        userDao.insert(user);

        // then
        assertThat(userDao.findByAccount(account)).isPresent();
    }

    @Test
    void update() {
        // given
        final String newPassword = "password99";
        final User findUser = userDao.findById(user.getId()).orElseThrow();
        findUser.changePassword(newPassword);

        // when
        userDao.update(findUser);

        // then
        final User actual = userDao.findById(user.getId()).orElseThrow();
        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
