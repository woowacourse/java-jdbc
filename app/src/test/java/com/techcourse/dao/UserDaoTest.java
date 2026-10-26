package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        jdbcTemplate.update("truncate table users restart identity");

        userDao = new UserDao(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        userDao.insert(new User("timo", "another-password", "timo@woowahan.com"));

        final var users = userDao.findAll();

        assertThat(users).extracting(User::getAccount).containsExactlyInAnyOrder("gugu", "timo");
        assertThat(users).extracting(User::getEmail)
                .containsExactlyInAnyOrder("hkkang@woowahan.com", "timo@woowahan.com");
    }

    @Test
    void findAllReturnsEmptyListWhenNoUsersExist() {
        jdbcTemplate.update("delete from users");

        assertThat(userDao.findAll()).isEmpty();
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
    void findByIdReturnsNullWhenUserDoesNotExist() {
        assertThat(userDao.findById(999L)).isNull();
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void findByAccountReturnsNullWhenUserDoesNotExist() {
        assertThat(userDao.findByAccount("missing")).isNull();
    }

    @Test
    void findByAccountTreatsSqlAsParameter() {
        assertThat(userDao.findByAccount("' or '1' = '1")).isNull();
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
    void insertAndFindAccountContainingQuote() {
        final var account = "timo's-account";
        userDao.insert(new User(account, "password", "timo@woowahan.com"));

        assertThat(userDao.findByAccount(account).getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        userDao.insert(new User("timo", "unchanged-password", "timo@woowahan.com"));
        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
        assertThat(actual.getAccount()).isEqualTo("gugu");
        assertThat(actual.getEmail()).isEqualTo("hkkang@woowahan.com");
        assertThat(userDao.findById(2L).getPassword()).isEqualTo("unchanged-password");
    }

    @Test
    void findsUserWithInjectedJdbcTemplate() {
        final var dao = new UserDao(jdbcTemplate);

        assertThat(dao.findById(1L).getAccount()).isEqualTo("gugu");
    }
}
