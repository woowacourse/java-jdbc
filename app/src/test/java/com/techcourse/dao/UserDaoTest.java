package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.exception.DataAccessException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserDaoTest {

    private static final Connection conn;

    static {
        try {
            conn = DataSourceConfig.getInstance().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(new JdbcTemplate(DataSourceConfig.getInstance()));
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(conn, user);
    }

    @Test
    void findAll() {
        final var users = userDao.findAll(conn);

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final var user = userDao.findById(conn, 1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(conn, account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(conn, user);

        final var actual = userDao.findById(conn, 2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(conn, 1L);
        user.changePassword(newPassword);

        userDao.update(conn, user);

        final var actual = userDao.findById(conn, 1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void deleteAll() {
        userDao.deleteAll(conn);

        List<User> users = userDao.findAll(conn);
        assertThat(users).isEmpty();
    }

    @Test
    void findObjectReturnNull() {
        userDao.deleteAll(conn);

        assertThatThrownBy(() -> userDao.findById(conn, 1L))
                .isInstanceOf(DataAccessException.class);
    }
}
