package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private UserDao userDao;
    private User savedUser;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        userDao = new UserDao(DataSourceConfig.getInstance());

        User user = new User("gugu", "password", "hkkang@woowahan.com");
        savedUser = userDao.insert(user);
    }

    @AfterEach
    void tearDown() {
        userDao.deleteAll();
    }

    @Test
    void insert() {
        // given
        String account = "insert-gugu";
        User user = new User(account, "password", "hkkang@woowahan.com");

        // when
        User insertedUser = userDao.insert(user);

        // then
        User actual = userDao.findById(insertedUser.getId());
        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void findAll() {
        List<User> users = userDao.findAll();

        assertThat(users).isNotEmpty();
        assertThat(users).containsExactly(savedUser);
    }

    @Test
    void findById() {
        User user = userDao.findById(savedUser.getId());

        assertThat(user).isEqualTo(savedUser);
    }

    @Test
    void findByAccount() {
        User user = userDao.findByAccount(savedUser.getAccount());

        assertThat(user).isEqualTo(savedUser);
    }

    @Test
    void update() {
        // given
        String newPassword = "password99";

        User beforeChangeUser = userDao.findById(savedUser.getId());
        assertThat(beforeChangeUser.getPassword()).isNotEqualTo(newPassword);

        // when
        beforeChangeUser.changePassword(newPassword);
        userDao.update(beforeChangeUser);

        // then
        User afterChangeUser = userDao.findById(savedUser.getId());
        assertThat(afterChangeUser.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void deleteAll() {
        // given
        List<User> users = userDao.findAll();
        assertThat(users).isNotEmpty();

        // when
        int deletedCount = userDao.deleteAll();

        // then
        assertThat(userDao.findAll()).isEmpty();
        assertThat(deletedCount).isEqualTo(users.size());
    }
}
