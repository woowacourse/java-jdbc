package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() throws SQLException {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        try (final var connection = DataSourceConfig.getInstance().getConnection();
             final var statement = connection.createStatement()) {
            statement.execute("truncate table users restart identity");
        }

        userDao = new UserDao(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        userDao.insert(new User("choco", "password99", "choco@example.com"));

        final var users = userDao.findAll();

        assertThat(users)
                .extracting(User::getAccount)
                .containsExactlyInAnyOrder("gugu", "choco");
    }

    @Test
    void findById() {
        final var user = userDao.findById(1L);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getAccount()).isEqualTo("gugu");
        assertThat(user.getPassword()).isEqualTo("password");
        assertThat(user.getEmail()).isEqualTo("hkkang@woowahan.com");
    }

    @Test
    void findByIdReturnsNullWhenNotFound() {
        assertThat(userDao.findById(999L)).isNull();
    }

    @Test
    void findByAccount() {
        userDao.insert(new User("choco", "password99", "choco@example.com"));

        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void findByAccountReturnsNullWhenNotFound() {
        assertThat(userDao.findByAccount("unknown")).isNull();
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void insertAndFindWithJdbcTemplate() {
        final var dao = new UserDao(new JdbcTemplate(DataSourceConfig.getInstance()));
        dao.insert(new User("template-gugu", "password", "template@example.com"));

        final var actual = dao.findByAccount("template-gugu");

        assertThat(actual.getAccount()).isEqualTo("template-gugu");
        assertThat(actual.getPassword()).isEqualTo("password");
        assertThat(actual.getEmail()).isEqualTo("template@example.com");
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
