package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() throws SQLException {
        final var dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        try (final var connection = dataSource.getConnection();
             final var statement = connection.createStatement()) {
            statement.execute("truncate table users restart identity");
        }

        userDao = new UserDao(dataSource);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        userDao.insert(new User("second", "second-password", "second@woowahan.com"));

        final var users = userDao.findAll();

        assertThat(users)
                .extracting(User::getId, User::getAccount, User::getPassword, User::getEmail)
                .containsExactlyInAnyOrder(
                        tuple(1L, "gugu", "password", "hkkang@woowahan.com"),
                        tuple(2L, "second", "second-password", "second@woowahan.com")
                );
    }

    @Test
    void findById() {
        userDao.insert(new User("second", "second-password", "second@woowahan.com"));

        final var user = userDao.findById(2L);

        assertThat(user).usingRecursiveComparison()
                .isEqualTo(new User(2L, "second", "second-password", "second@woowahan.com"));
    }

    @Test
    void findByAccount() {
        final var account = "second";
        userDao.insert(new User(account, "second-password", "second@woowahan.com"));

        final var user = userDao.findByAccount(account);

        assertThat(user).usingRecursiveComparison()
                .isEqualTo(new User(2L, account, "second-password", "second@woowahan.com"));
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(2L);

        assertThat(actual).usingRecursiveComparison()
                .isEqualTo(new User(2L, account, "password", "hkkang@woowahan.com"));
    }

    @Test
    void update() {
        userDao.insert(new User("second", "second-password", "second@woowahan.com"));
        final var newPassword = "password99";
        final var user = userDao.findById(2L);
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(2L);

        assertThat(actual).usingRecursiveComparison()
                .isEqualTo(new User(2L, "second", newPassword, "second@woowahan.com"));
        assertThat(userDao.findById(1L)).usingRecursiveComparison()
                .isEqualTo(new User(1L, "gugu", "password", "hkkang@woowahan.com"));
    }

    @Test
    void returnsNullWhenUserDoesNotExist() {
        assertThat(userDao.findById(999L)).isNull();
        assertThat(userDao.findByAccount("missing")).isNull();
    }

    @Test
    void treatsAccountContainingQuotesAsAParameter() {
        final var account = "gugu' OR '1'='1";
        userDao.insert(new User(account, "password", "quoted@woowahan.com"));

        final var actual = userDao.findByAccount(account);

        assertThat(actual).usingRecursiveComparison()
                .isEqualTo(new User(2L, account, "password", "quoted@woowahan.com"));
    }

    @Test
    void acceptsJdbcTemplate() {
        final var dao = new UserDao(new JdbcTemplate(DataSourceConfig.getInstance()));
        dao.insert(new User("template", "password", "template@woowahan.com"));

        assertThat(dao.findByAccount("template")).usingRecursiveComparison()
                .isEqualTo(new User(2L, "template", "password", "template@woowahan.com"));
    }
}
