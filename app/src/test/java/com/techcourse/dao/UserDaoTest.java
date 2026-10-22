package com.techcourse.dao;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserDaoTest {

    private UserDao userDao;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        final var dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("truncate table user_history restart identity");
        jdbcTemplate.update("truncate table users restart identity");

        userDao = new UserDao(dataSource);
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
    }

    @Test
    void findAll() {
        userDao.insert(new User("brown", "brown-password", "brown@woowahan.com"));

        final var users = userDao.findAll();

        assertThat(users).extracting(User::getAccount).containsExactlyInAnyOrder("gugu", "brown");
        assertUser(users.stream().filter(user -> user.getAccount().equals("gugu")).findFirst().orElseThrow(),
                1L, "gugu", "password", "hkkang@woowahan.com");
        assertUser(users.stream().filter(user -> user.getAccount().equals("brown")).findFirst().orElseThrow(),
                2L, "brown", "brown-password", "brown@woowahan.com");
    }

    @Test
    void findAllReturnsEmptyListWhenNoUsersExist() {
        jdbcTemplate.update("delete from users");

        assertThat(userDao.findAll()).isEmpty();
    }

    @Test
    void findById() {
        assertUser(userDao.findById(1L), 1L, "gugu", "password", "hkkang@woowahan.com");
    }

    @Test
    void findByAccount() {
        assertUser(userDao.findByAccount("gugu"), 1L, "gugu", "password", "hkkang@woowahan.com");
    }

    @Test
    void missingUserReturnsNull() {
        assertThat(userDao.findById(999L)).isNull();
        assertThat(userDao.findByAccount("missing")).isNull();
    }

    @Test
    void insert() {
        userDao.insert(new User("insert-gugu", "new-password", "insert@woowahan.com"));

        assertUser(userDao.findById(2L), 2L, "insert-gugu", "new-password", "insert@woowahan.com");
    }

    @Test
    void update() {
        final var user = userDao.findById(1L);
        user.changePassword("password99");

        userDao.update(user);

        assertUser(userDao.findById(1L), 1L, "gugu", "password99", "hkkang@woowahan.com");
    }

    @Test
    void updateChangesAllFieldsOnlyForMatchingId() {
        userDao.insert(new User("brown", "brown-password", "brown@woowahan.com"));

        userDao.update(new User(1L, "renamed-gugu", "changed-password", "changed@woowahan.com"));

        assertUser(userDao.findById(1L), 1L, "renamed-gugu", "changed-password", "changed@woowahan.com");
        assertUser(userDao.findById(2L), 2L, "brown", "brown-password", "brown@woowahan.com");
    }

    @Test
    void jdbcTemplateConstructorSupportsWritesAndReads() {
        final var dao = new UserDao(jdbcTemplate);
        dao.insert(new User("template", "before", "template@woowahan.com"));
        final var user = dao.findByAccount("template");
        user.changePassword("after");

        dao.update(user);

        assertUser(dao.findById(user.getId()), 2L, "template", "after", "template@woowahan.com");
        assertThat(dao.findAll()).hasSize(2);
    }

    @Test
    void accountParametersAreTreatedAsData() {
        final var account = "gugu' OR '1' = '1";
        assertThat(userDao.findByAccount(account)).isNull();

        userDao.insert(new User(account, "quoted-password", "quoted@woowahan.com"));

        assertUser(userDao.findByAccount(account), 2L, account, "quoted-password", "quoted@woowahan.com");
        assertThat(userDao.findAll()).hasSize(2);
    }

    @Test
    void duplicateAccountsAreRejectedBySingleRowLookup() {
        userDao.insert(new User("gugu", "another-password", "another@woowahan.com"));

        assertThatThrownBy(() -> userDao.findByAccount("gugu"))
                .isInstanceOf(DataAccessException.class);
    }

    private void assertUser(final User user, final long id, final String account,
                            final String password, final String email) {
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getAccount()).isEqualTo(account);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getEmail()).isEqualTo(email);
    }
}
