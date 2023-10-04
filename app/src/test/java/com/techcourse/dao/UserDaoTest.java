package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private Long defaultId;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(DataSourceConfig.getInstance());
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        jdbcTemplate.update("delete from users");
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
        defaultId = jdbcTemplate.queryForObject("select id from users where account = ?", resultSet -> resultSet.getLong("id"), "gugu");
    }

    @Test
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final var user = userDao.findById(defaultId);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        Long insertedId = jdbcTemplate.queryForObject("select id from users where account = ?", resultSet -> resultSet.getLong("id"), account);
        final var actual = userDao.findById(insertedId);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(defaultId);
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(defaultId);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
