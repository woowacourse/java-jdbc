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
        final var users = userDao.findAll();

        assertThat(users).extracting(User::getAccount).containsExactly("gugu");
    }

    @Test
    void findById() {
        final var user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getPassword()).isEqualTo("password");
        assertThat(user.getEmail()).isEqualTo("hkkang@woowahan.com");
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
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
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
        assertThat(actual.getAccount()).isEqualTo("gugu");
        assertThat(actual.getEmail()).isEqualTo("hkkang@woowahan.com");
    }

    @Test
    void acceptsJdbcTemplate() {
        final var anotherDao = new UserDao(new JdbcTemplate(DataSourceConfig.getInstance()));
        anotherDao.insert(new User("brown", "password", "brown@example.com"));

        assertThat(anotherDao.findByAccount("brown").getEmail()).isEqualTo("brown@example.com");
        assertThat(userDao.findAll()).extracting(User::getAccount).containsExactly("gugu", "brown");
    }

    @Test
    void findAllReturnsEmptyListWhenNoUsersExist() {
        new JdbcTemplate(DataSourceConfig.getInstance()).update("delete from users");

        assertThat(userDao.findAll()).isEmpty();
    }

    @Test
    void missingUserReturnsNull() {
        assertThat(userDao.findById(999L)).isNull();
        assertThat(userDao.findByAccount("missing")).isNull();
    }

    @Test
    void findByAccountTreatsSqlAsValue() {
        final var account = "gugu' OR '1'='1";
        userDao.insert(new User(account, "password", "quoted@example.com"));

        assertThat(userDao.findByAccount(account).getEmail()).isEqualTo("quoted@example.com");
    }

    @Test
    void updateOnlyChangesTargetUser() {
        userDao.insert(new User("brown", "password", "brown@example.com"));
        userDao.update(new User(1L, "changed", "new-password", "new@example.com"));

        final var actual = userDao.findById(1L);
        assertThat(actual.getAccount()).isEqualTo("changed");
        assertThat(actual.getPassword()).isEqualTo("new-password");
        assertThat(actual.getEmail()).isEqualTo("new@example.com");
        assertThat(userDao.findById(2L).getPassword()).isEqualTo("password");
    }
}
