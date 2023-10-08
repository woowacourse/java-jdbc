package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class UserDaoTest {

    private UserDao userDao;
    private final DataSource dataSource = DataSourceConfig.getInstance();
    private Connection connection;

    @BeforeEach
    void setup() throws SQLException {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        connection = dataSource.getConnection();

        userDao = new UserDao(new JdbcTemplate(DataSourceConfig.getInstance()));
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(connection, user);
    }

    @Test
    void findAll() throws SQLException {
        System.out.println(connection);
        userDao.insert(connection, new User("millie", "password", "email@email.com"));
        System.out.println(connection);
        final var users = userDao.findAll(connection);
        System.out.println(connection);

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() throws SQLException {
        final var user = userDao.findById(connection, 1L).get();

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() throws SQLException {
        final var account = "gugu2";
        userDao.insert(connection, new User(account, "password", "email@email.com"));
        final var user = userDao.findByAccount(connection, account).get();

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() throws SQLException {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(connection, user);

        final var actual = userDao.findById(connection, 2L).get();

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() throws SQLException {
        final var newPassword = "password99";
        final var user = userDao.findById(connection, 1L).get();
        user.changePassword(newPassword);

        userDao.update(connection, user);

        final var actual = userDao.findById(connection, 1L).get();

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @AfterEach
    void after() {
        try {
            connection.close();
        } catch (final SQLException ignored) {
        }
    }
}
