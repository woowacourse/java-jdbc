package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(DataSourceConfig.getInstance());
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        //when
        List<User> users = userDao.findAll();

        //then
        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        // when
        Optional<User> optionalUser = userDao.findById(1L);

        // then
        assertAll(
                () -> assertThat(optionalUser).isPresent(),
                () -> assertThat(optionalUser.get().getAccount()).isEqualTo("gugu")
        );
    }

    @Test
    void findByAccount() {
        //given
        User target = new User("ash", "password", "test@techcourse.com");
        userDao.insert(target);
        String account = target.getAccount();

        //when
        Optional<User> optionalUser = userDao.findByAccount(account);

        //then
        assertAll(
                () -> assertThat(optionalUser).isPresent(),
                () -> assertThat(optionalUser.get().getAccount()).isEqualTo(account)
        );
    }

    @Test
    void insert() {
        //given
        String account = "insert-gugu";
        User user = new User(account, "password", "hkkang@woowahan.com");

        //when
        userDao.insert(user);
        Optional<User> optionalUser = userDao.findById(2L);

        //then
        assertAll(
                () -> assertThat(optionalUser).isPresent(),
                () -> assertThat(optionalUser.get().getAccount()).isEqualTo(account)
        );
    }

    @Test
    void update() throws SQLException {
        //given
        DataSource dataSource = DataSourceConfig.getInstance();
        String newPassword = "password99";
        Optional<User> user = userDao.findById(1L);
        User initialUser = user.get();

        //when
        initialUser.changePassword(newPassword);
        userDao.update(initialUser);

        Optional<User> optionalUser = userDao.findById(1L);

        //then
        assertAll(
                () -> assertThat(optionalUser).isPresent(),
                () -> assertThat(optionalUser.get().getPassword()).isEqualTo(newPassword)
        );
    }
}
