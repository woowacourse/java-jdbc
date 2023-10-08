package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class UserDaoTest {

    private static final String INIT_USER_TABLE_SQL = "DROP TABLE IF EXISTS users; "
            + " "
            + "create table if not exists users ("
            + "    id bigint auto_increment,"
            + "    account varchar(100) not null,"
            + "    password varchar(100) not null,"
            + "    email varchar(100) not null,"
            + "    primary key(id)"
            + ");";

    private UserDao userDao;

    private final DataSource dataSource = DataSourceConfig.getInstance();

    private Connection connection;

    @BeforeEach
    void setup() throws SQLException {
        connection = dataSource.getConnection();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute(connection, INIT_USER_TABLE_SQL);

        DatabasePopulatorUtils.execute(dataSource);
        userDao = new UserDao(dataSource);

        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(connection, user);
    }

    @Test
    void findAll() {
        final var users = userDao.findAll(connection);

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final var user = userDao.findById(connection, 1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findById_fail() {
        assertThatThrownBy(
                () -> userDao.findById(connection, -1L)
        ).isInstanceOf(NoSuchElementException.class)
                .hasMessage("id에 해당하는 user가 없습니다.");
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(connection, account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void findByAccount_fail() {
        assertThatThrownBy(
                () -> userDao.findByAccount(connection, "joy")
        ).isInstanceOf(NoSuchElementException.class)
                .hasMessage("account에 해당하는 user가 없습니다.");
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
