package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.exception.UserNotFoundException;
import com.techcourse.exception.UserUpdateFailureException;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

class UserDaoTest {

    private UserDao userDao;
    private User savedUser;
    private Long savedUserId;
    private final DataSource dataSource = new DataSourceConfig().dataSource();

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(dataSource);

        userDao = new UserDao(dataSource);
        savedUser = userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
        savedUserId = savedUser.getId();
    }

    @AfterEach
    void tearDown() {
        userDao.deleteAll();
    }

    @Test
    void findAll() {
        // when
        List<User> users = userDao.findAll();

        // then
        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        // when
        Optional<User> user = userDao.findById(savedUserId);

        // then
        assertThat(user).isNotEmpty();
        assertThat(user.get().getAccount()).isEqualTo(savedUser.getAccount());
    }

    @Test
    void findByAccount() {
        // when
        Optional<User> user = userDao.findByAccount(savedUser.getAccount());

        // then
        assertThat(user).isNotEmpty();
        assertThat(user.get().getAccount()).isEqualTo(savedUser.getAccount());
    }

    @Test
    void insert() {
        // given
        String account = "insert-gugu";
        User user = new User(account, "password", "hkkang@woowahan.com");

        // when
        User insertedUser = userDao.insert(user);

        // then
        assertThat(insertedUser.getAccount()).isEqualTo(account);
        assertThat(insertedUser.getId()).isEqualTo(savedUserId + 1L);
    }

    @Test
    void update() {
        // given
        String newPassword = "password99";
        User user = userDao.findById(savedUserId)
            .orElseThrow(() -> new UserNotFoundException(savedUserId));
        user.changePassword(newPassword);

        // when
        userDao.update(user);

        // then
        Optional<User> actual = userDao.findById(savedUserId);
        assertThat(actual).isNotEmpty();
        assertThat(actual.get().getPassword()).isEqualTo(newPassword);
    }

    @Test
    void updateWithWrongId() {
        // given
        User user = userDao.findById(savedUserId)
            .orElseThrow(() -> new UserNotFoundException(savedUserId));
        User nonExistingUser = new User(
            user.getId() + 1L,
            user.getAccount(),
            user.getPassword(),
            user.getEmail()
        );

        // when // then
        assertThatThrownBy(() -> userDao.update(nonExistingUser))
            .isExactlyInstanceOf(UserUpdateFailureException.class);
    }

    @Test
    void deleteById() {
        // given
        User user = new User(
            "mak9hyeon",
            "hyeon9mak babo",
            "9mak@woowahan.com");
        User insertedUser = userDao.insert(user);

        // when // then
        assertThatCode(() -> userDao.deleteById(insertedUser.getId()))
            .doesNotThrowAnyException();
    }

    @Test
    void deleteAll() {
        // when
        userDao.deleteAll();

        // then
        List<User> actual = userDao.findAll();
        assertThat(actual).isEmpty();
    }
}
