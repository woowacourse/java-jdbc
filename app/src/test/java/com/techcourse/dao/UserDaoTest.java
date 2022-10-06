package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import nextstep.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        userDao = new UserDao(new JdbcTemplate(DataSourceConfig.getInstance()));

        var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        var user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        var lala = new User("lala", "password", "lala@woowahan.com");
        userDao.insert(lala);
        var user = userDao.findByAccount(lala.getAccount());

        assertThat(user.getAccount()).isEqualTo(lala.getAccount());
    }

    @Test
    void insert() {
        var account = "insert-gugu";
        var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        var actual = userDao.findById(2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        var newPassword = "password99";
        var user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
