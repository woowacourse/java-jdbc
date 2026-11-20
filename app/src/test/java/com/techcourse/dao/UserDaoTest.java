package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.dao.DataAccessException;
import org.h2.jdbcx.JdbcDataSource;
import java.util.UUID;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        final var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:user-dao-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(dataSource);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final var user = userDao.findById(1L).orElseThrow();

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account).orElseThrow();

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void returnsEmptyWhenUserDoesNotExist() {
        assertThat(userDao.findById(999L)).isEmpty();
        assertThat(userDao.findByAccount("missing")).isEmpty();
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(2L).orElseThrow();

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L).orElseThrow();
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L).orElseThrow();

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
    @Test
    void rejectsDuplicateAccountResults() {
        userDao.insert(new User("gugu", "another-password", "another@example.com"));

        assertThatThrownBy(() -> userDao.findByAccount("gugu"))
                .isInstanceOf(DataAccessException.class);
    }
    @Test
    void passwordUpdatePreservesChangedEmail() {
        final var oldUser = userDao.findById(1L).orElseThrow();
        userDao.update(new User(1L, oldUser.getAccount(), oldUser.getPassword(), "changed@example.com"));

        userDao.updatePassword(oldUser.getId(), "newPassword");

        final var actual = userDao.findById(1L).orElseThrow();
        assertThat(actual.getEmail()).isEqualTo("changed@example.com");
        assertThat(actual.getPassword()).isEqualTo("newPassword");
    }
}
