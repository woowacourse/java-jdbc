package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;
    private User user;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(DataSourceConfig.getInstance());
        user = new User("gugu", "password", "hkkang@woowahan.com");
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
    @DisplayName("userDao에 등록된 모든 유저를 조회한다. - 데이터의 개수가 0개일 때")
    void findAllWhenZero() {
        userDao.deleteAll();

        // given - when
        final List<User> users = userDao.findAll();
        // then
        assertThat(users).isEmpty();
    }

    @Test
    @DisplayName("userDao에 등록된 모든 유저를 조회한다. - 데이터의 개수가 하나일 때")
    void findAll() {
        final List<User> users = userDao.findAll();

        assertThat(users).isNotEmpty();
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getId()).isNotNull();
        assertThat(users.get(0).getAccount()).isEqualTo(user.getAccount());
        assertThat(users.get(0).getPassword()).isEqualTo(user.getPassword());
    }

    @Test
    @DisplayName("userDao에 등록된 모든 유저를 조회한다. - 데이터의 개수가 두개 이상일 때")
    void findAllWhenMoreThanOne() {
        userDao.insert(new User("joanne", "1234", "joanne@woowahan.com"));
        final List<User> users = userDao.findAll();

        assertThat(users).isNotEmpty();
        assertThat(users).hasSize(2);
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
