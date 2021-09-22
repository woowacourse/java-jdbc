package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.lang.reflect.InvocationTargetException;
import nextstep.jdbc.test.Rollback;
import nextstep.jdbc.utils.TransactionManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(Rollback.class)
class UserDaoTest {

    private static UserDao userDao;

    @BeforeAll
    static void beforeAll() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        userDao = new UserDao(DataSourceConfig.getInstance());
    }

    @BeforeEach
    void setup() {
        userDao.removeAll();
    }

    @Test
    void findAll() {
        유저_등록();
        final List<User> users = userDao.findAll();

        assertThat(users).extracting(User::getAccount)
            .containsExactlyInAnyOrder("gugu", "nabom");
    }

    @Test
    void findByAccount() {
        유저_등록();
        final String account = "gugu";
        final User user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void findById() {
        유저_등록();
        final Long userId = userDao.findByAccount("gugu").getId();
        final User foundUser = userDao.findById(userId);

        assertThat(foundUser.getAccount()).isEqualTo("gugu");
    }

    @Test
    void insert() {
        final String account = "insert-gugu";
        final User user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final User actual = userDao.findByAccount(account);

        assertThat(actual.getAccount()).isNotEmpty();
    }

    @Test
    void update() {
        유저_등록();
        final String newPassword = "password99";
        final User user = userDao.findByAccount("gugu");
        user.changePassword(newPassword);

        userDao.update(user);

        final User actual = userDao.findByAccount("gugu");

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    void 유저_등록() {
        final User user1 = new User("gugu", "password", "hkkang@woowahan.com");
        final User user2 = new User("nabom", "password", "nabom@woowahan.com");
        userDao.insert(user1);
        userDao.insert(user2);
    }
}
