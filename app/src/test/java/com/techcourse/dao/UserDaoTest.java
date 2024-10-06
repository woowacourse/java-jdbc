package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());

        userDao = new UserDao(jdbcTemplate);
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    @DisplayName("user의 모든 정보를 가져올 수 있다.")
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    @DisplayName("단일 user의 정보를 id로 가져올 수 있다.")
    void findById() {
        final var user = userDao.findById(1L).get();

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    @DisplayName("단일 user의 정보를 account로 가져올 수 있다.")
    void findByAccount() {
        userDao.insert(new User("polla", "password", "polla@gmail.com"));

        final var account = "polla";
        final var user = userDao.findByAccount(account).get();

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    @DisplayName("새로운 유저를 저장할 수 있다.")
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(2L).get();

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    @DisplayName("유저의 password를 변경할 수 있다.")
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L).get();
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L).get();

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
