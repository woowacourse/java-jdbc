package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.SQLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(DataSourceConfig.getInstance());
        final User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @AfterEach
    void tearDown() {
        userDao.deleteAll();
    }

    @Test
    @DisplayName("user를 삽입한다.")
    void insert() {
        final String account = "insert-gugu";
        final User user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final User actual = userDao.findById(2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    @DisplayName("user의 비밀번호를 수정한다.")
    void update() {
        final String newPassword = "password99";
        final User user = userDao.findByAccount("gugu");
        user.changePassword(newPassword);

        userDao.update(user);

        final User actual = userDao.findByAccount("gugu");

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void findAll() {
        final List<User> users = userDao.findAll();

        assertThat(users).isNotEmpty();
        assertThat(users).hasSize(1);
    }

    @Test
    void findById() {
        final User user = userDao.findById(
            userDao.findByAccount("gugu").getId()
        );

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final String account = "gugu";
        final User user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @DisplayName("등록된 모든 user를 삭제한다.")
    @Test
    void deleteAll() {
        // given
        assertThat(userDao.findAll()).isNotEmpty();

        // when
        userDao.deleteAll();

        // then
        assertThat(userDao.findAll()).isEmpty();
    }
}
