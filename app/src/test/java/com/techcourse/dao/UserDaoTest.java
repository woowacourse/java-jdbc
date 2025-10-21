package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private static final DataSource dataSource = DataSourceConfig.getInstance();
    private UserDao userDao;

    @BeforeEach
    void setup() throws SQLException {
        DatabasePopulatorUtils.execute(dataSource);

        userDao = new UserDao(new JdbcTemplate(dataSource));
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(dataSource.getConnection(), user);
    }

    @AfterEach
    void after() throws SQLException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update(dataSource.getConnection(), "delete from users");
    }

    @Test
    void findAll() throws SQLException {
        final var users = userDao.findAll(dataSource.getConnection());

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() throws SQLException {
        User gugu = userDao.findByAccount(dataSource.getConnection(), "gugu");
        final var user = userDao.findById(dataSource.getConnection(), gugu.getId());

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() throws SQLException {
        final var account = "gugu";
        final var user = userDao.findByAccount(dataSource.getConnection(), account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() throws SQLException {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(dataSource.getConnection(), user);

        final var actual = userDao.findById(dataSource.getConnection(), 2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() throws SQLException {
        final var newPassword = "password99";
        final var user = userDao.findByAccount(dataSource.getConnection(), "gugu");
        user.changePassword(newPassword);

        userDao.update(dataSource.getConnection(), user);

        final var actual = userDao.findById(dataSource.getConnection(), 1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
