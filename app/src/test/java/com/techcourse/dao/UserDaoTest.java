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
    private Connection connection;
    private JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());

    @BeforeEach
    void setup() throws SQLException {
        DataSource dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);

        connection = dataSource.getConnection();
        userDao = new UserDao(dataSource);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(connection, user);
    }

    @AfterEach
    void initTable() throws SQLException {
        connection.close();
        jdbcTemplate.execute("delete from users");
        jdbcTemplate.execute("alter table users alter column id restart with 1");
    }

    @Test
    void findAll() throws SQLException {
        final var users = userDao.findAll(connection);

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final var user = userDao.findById(connection, 1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(connection, account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(connection, user);

        final var actual = userDao.findById(connection, 2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(connection, 1L);
        user.changePassword(newPassword);

        userDao.update(connection, user);

        final var actual = userDao.findById(connection, 1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
