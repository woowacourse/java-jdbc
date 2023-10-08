package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class UserDaoTest {

    private UserDao userDao;
    private final DataSource dataSource = DataSourceConfig.getInstance();

    @BeforeEach
    void setup() throws SQLException {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(new JdbcTemplate(DataSourceConfig.getInstance()));
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(dataSource.getConnection(), user);
    }

    @Test
    void findAll() throws SQLException {
        userDao.insert(dataSource.getConnection(), new User("millie", "password", "email@email.com"));
        final var users = userDao.findAll(dataSource.getConnection());

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() throws SQLException {
        final var user = userDao.findById(dataSource.getConnection(), 1L).get();

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() throws SQLException {
        final var account = "gugu2";
        userDao.insert(dataSource.getConnection(), new User(account, "password", "email@email.com"));
        final var user = userDao.findByAccount(dataSource.getConnection(), account).get();

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() throws SQLException {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(dataSource.getConnection(), user);

        final var actual = userDao.findById(dataSource.getConnection(), 2L).get();

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() throws SQLException {
        final var newPassword = "password99";
        final var user = userDao.findById(dataSource.getConnection(), 1L).get();
        user.changePassword(newPassword);

        userDao.update(dataSource.getConnection(), user);

        final var actual = userDao.findById(dataSource.getConnection(), 1L).get();

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
