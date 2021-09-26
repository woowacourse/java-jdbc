package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private static UserDao userDao;

    @BeforeAll
    static void beforeAll() {
        DataSource dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);

        userDao = new UserDao(dataSource);

        유저등록("gugu", "password", "hkkang@woowahan.com");
    }

    @Test
    void insert() {
        유저등록("gugu-insert", "password", "hkkang@woowahan.com");

        final User actual = userDao.findById(2L);

        assertThat(actual.getAccount()).isEqualTo("gugu-insert");
    }

    @Test
    void update() {
        final String newPassword = "password99";
        final User user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        final User actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void findAll() {
        final List<User> users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final User user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final String account = "gugu";
        final User user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    private static void 유저등록(String account, String password, String email) {
        final User user = new User(account, password, email);
        userDao.insert(user);
    }
}
