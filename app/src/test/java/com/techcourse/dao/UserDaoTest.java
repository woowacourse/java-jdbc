package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import nextstep.jdbc.DatabasePopulatorUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class UserDaoTest {

    private UserDao userDao;

    private User gugu;
    private User josh;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(DataSourceConfig.getInstance());
        gugu = new User("gugu", "password", "hkkang@woowahan.com");
        josh = new User("josh", "password", "whgusrms96@gmail.com");

        userDao.insert(gugu);
        userDao.insert(josh);
    }

    @AfterEach
    void refresh() {
        userDao.deleteAll();
    }

    @Test
    void findAll() {
        final var users = userDao.findAll();

        assertAll(
                () -> assertThat(users.size()).isEqualTo(2),
                () -> assertThat(users).contains(gugu),
                () -> assertThat(users).contains(josh)
        );
    }

    @Test
    void findById() {
        final var user = userDao.findById(gugu.getId());

        assertThat(user.getAccount()).isEqualTo("gugu");
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

        final var actual = userDao.findById(user.getId());

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(gugu.getId());
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(gugu.getId());

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
