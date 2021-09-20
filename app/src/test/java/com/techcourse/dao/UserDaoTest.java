package com.techcourse.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;

import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nextstep.jdbc.datasource.DataSourceConfig;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private final DataSource dataSource = DataSourceConfig.getInstance();
    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(dataSource);

        userDao = new UserDao(DataSourceConfig.getInstance());
        final User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("drop table users");
        }
    }

    @Test
    void findAll() {
        final User user = new User("air", "password", "air.junseo@gmail.com");
        userDao.insert(user);

        final List<User> users = userDao.findAll();

        assertThat(users).isNotEmpty();
        assertThat(users).hasSize(2);
    }

    @Test
    void findById() {
        final User user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final String account = "gugu";
        final User user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final String account = "insert-gugu";
        final User user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final User actual = userDao.findById(2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final String newPassword = "password99";
        final User user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        final User actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
