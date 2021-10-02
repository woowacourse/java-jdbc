package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.exception.UserNotFoundException;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private UserDao userDao;
    private User savedUser;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao();
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
        savedUser = userDao.findByAccount("gugu")
            .orElseThrow(() -> new UserNotFoundException("gugu"));
    }

    @AfterEach
    void tearDown() {
        userDao.deleteById(savedUser.getId());
    }

    @Test
    void findAll() {
        List<User> users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        Optional<User> user = userDao.findById(savedUser.getId());

        assertThat(user).isNotEmpty();
        assertThat(user.get().getAccount()).isEqualTo(savedUser.getAccount());
    }

    @Test
    void findByAccount() {
        Optional<User> user = userDao.findByAccount(savedUser.getAccount());

        assertThat(user).isNotEmpty();
        assertThat(user.get().getAccount()).isEqualTo(savedUser.getAccount());
    }

    @Test
    void insert() {
        String account = "insert-gugu";
        User user = new User(account, "password", "hkkang@woowahan.com");

        userDao.insert(user);

//        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        String newPassword = "password99";
        User user = userDao.findById(savedUser.getId())
            .orElseThrow(() -> new UserNotFoundException(savedUser.getId()));
        user.changePassword(newPassword);

        userDao.update(user);

        Optional<User> actual = userDao.findById(savedUser.getId());

        assertThat(actual).isNotEmpty();
        assertThat(actual.get().getPassword()).isEqualTo(newPassword);
    }

    @Test
    void delete() {
        Long deletingId = 1L;

        userDao.deleteById(1L);

        Optional<User> user = userDao.findById(deletingId);
        assertThat(user).isEmpty();
    }
}
