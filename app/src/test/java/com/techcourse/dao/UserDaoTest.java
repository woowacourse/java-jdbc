package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.List;
import java.util.Optional;
import nextstep.exception.DataAccessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(DataSourceConfig.getInstance());
        final User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        final List<User> users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final User user = userDao.findAll().stream()
            .findAny()
            .orElseThrow();

        User actual = userDao.findById(user.getId())
            .orElseThrow();

        assertThat(actual.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        userDao.insert(new User("guguByAccount", "password", "hkkang@woowahan.com"));
        final String account = "guguByAccount";
        final User user = userDao.findByAccount(account)
            .orElseThrow();

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void errorFindingObject() {
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
        assertThatThrownBy(() -> userDao.findByAccount("gugu"))
            .isInstanceOf(DataAccessException.class);
    }

    @Test
    void insert() {
        final String account = "insert-gugu";
        final User user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        Optional<User> actual = userDao.findAll().stream()
            .filter(it -> it.getAccount().equals(account))
            .findAny();

        assertThat(actual.get()).isNotNull();
    }

    @Test
    void update() {
        final String newPassword = "password99";
        final User user = userDao.findByAccount("gugu")
            .orElseThrow();
        user.changePassword(newPassword);

        userDao.update(user);

        final User actual = userDao.findById(user.getId())
            .orElseThrow();

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @AfterEach
    void tearDown() {
        userDao.removeAll();
    }
}
