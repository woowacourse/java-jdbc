package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        userDao.removeAll();
    }

    @Test
    void findAll() {
        final List<User> users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @DisplayName("findAll 에서 내부의 값을 확인한다.")
    @Test
    void findAllItems() {
        User better = new User("better", "password", "better@email.com");
        userDao.insert(better);

        final List<User> users = userDao.findAll();
        List<String> userNames = users.stream()
                .map(User::getAccount)
                .collect(Collectors.toList());

        assertThat(users)
                .isNotEmpty()
                .hasSize(2);
        assertThat(userNames).contains("gugu", "better");
    }

    @Test
    void findById() {
        User gugu = userDao.findByAccount("gugu");
        final User actual = userDao.findById(gugu.getId());

        assertThat(actual.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final String account = "gugu";
        final User user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @DisplayName("없는 account 로 찾으면 null 을 리턴한다.")
    @Test
    void findByAccountFail() {
        final String account = "gugu11";

        assertThat(userDao.findByAccount(account)).isNull();
    }

    @Test
    void insert() {
        final String account = "insert-gugu";
        final User user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final User actual = userDao.findByAccount(account);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final String newPassword = "password99";
        final User user = userDao.findByAccount("gugu");
        user.changePassword(newPassword);

        userDao.update(user);

        final User actual = userDao.findById(user.getId());

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @DisplayName("전체 삭제를 한다.")
    @Test
    void removeAll() {
        List<User> users = userDao.findAll();
        assertThat(users).isNotEmpty();

        userDao.removeAll();
        List<User> actual = userDao.findAll();

        assertThat(actual).isEmpty();
    }
}
