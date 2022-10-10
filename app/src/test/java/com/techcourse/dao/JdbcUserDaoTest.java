package com.techcourse.dao;

import static org.assertj.core.api.Assertions.*;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.techcourse.TestDataUtils;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;

import nextstep.jdbc.JdbcTemplate;

class JdbcUserDaoTest {

    private final DataSource dataSource = DataSourceConfig.getInstance();
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    private JdbcUserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(dataSource);
        userDao = new JdbcUserDao(dataSource);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.save(user);
    }

    @AfterEach
    void teardown() {
        TestDataUtils.h2TruncateTables(jdbcTemplate, "users");
    }

    @Test
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final var user = userDao.findById(1L);

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
        final var account = "save-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.save(user);

        final var actual = userDao.findById(2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
