package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() throws SQLException {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        clearUsers();

        userDao = new UserDao(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        userDao.insert(new User("other", "otherPassword", "other@example.com"));

        final var users = userDao.findAll();

        assertThat(users)
                .extracting(User::getId, User::getAccount, User::getPassword, User::getEmail)
                .containsExactlyInAnyOrder(
                        tuple(1L, "gugu", "password", "hkkang@woowahan.com"),
                        tuple(2L, "other", "otherPassword", "other@example.com"));
    }

    @Test
    void findById() {
        final var user = userDao.findById(1L);

        assertThat(user).usingRecursiveComparison()
                .isEqualTo(new User(1L, "gugu", "password", "hkkang@woowahan.com"));
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user).usingRecursiveComparison()
                .isEqualTo(new User(1L, account, "password", "hkkang@woowahan.com"));
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
        userDao.insert(new User("other", "otherPassword", "other@example.com"));
        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L);

        assertThat(actual).usingRecursiveComparison()
                .isEqualTo(new User(1L, "gugu", newPassword, "hkkang@woowahan.com"));
        assertThat(userDao.findById(2L).getPassword()).isEqualTo("otherPassword");
    }

    @Test
    void updateSavesAllUserFields() {
        final var updated = new User(1L, "changed", "changedPassword", "changed@example.com");

        userDao.update(updated);

        assertThat(userDao.findById(1L)).usingRecursiveComparison().isEqualTo(updated);
    }

    @Test
    void findAllWhenEmpty() throws SQLException {
        clearUsers();

        assertThat(userDao.findAll()).isEmpty();
    }

    @Test
    void findByMissingId() {
        assertThat(userDao.findById(999L)).isNull();
    }

    @Test
    void findByMissingAccount() {
        assertThat(userDao.findByAccount("missing")).isNull();
    }

    @Test
    void duplicateAccountFailsSingleUserLookup() {
        userDao.insert(new User("gugu", "otherPassword", "other@example.com"));

        assertThatThrownBy(() -> userDao.findByAccount("gugu"))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void accountContainingSqlIsTreatedAsData() {
        final var account = "' or '1' = '1";
        userDao.insert(new User(account, "quotedPassword", "quoted@example.com"));

        assertThat(userDao.findByAccount(account)).usingRecursiveComparison()
                .isEqualTo(new User(2L, account, "quotedPassword", "quoted@example.com"));
        assertThat(userDao.findById(1L).getAccount()).isEqualTo("gugu");
    }

    @Test
    void jdbcTemplateConstructorUsesProvidedTemplate() {
        final var dao = new UserDao(new JdbcTemplate(DataSourceConfig.getInstance()));
        dao.insert(new User("template", "templatePassword", "template@example.com"));

        assertThat(dao.findById(2L)).usingRecursiveComparison()
                .isEqualTo(new User(2L, "template", "templatePassword", "template@example.com"));
    }

    @Test
    void separateDaoCanReadSavedUser() {
        final var anotherDao = new UserDao(DataSourceConfig.getInstance());

        assertThat(anotherDao.findById(1L)).usingRecursiveComparison()
                .isEqualTo(new User(1L, "gugu", "password", "hkkang@woowahan.com"));
    }

    @Test
    void sqlFailureIsTranslated() {
        assertThatThrownBy(() -> userDao.insert(new User(null, "password", "email@example.com")))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);
    }

    private void clearUsers() throws SQLException {
        try (final var connection = DataSourceConfig.getInstance().getConnection();
             final var statement = connection.createStatement()) {
            statement.execute("truncate table users restart identity");
        }
    }
}
