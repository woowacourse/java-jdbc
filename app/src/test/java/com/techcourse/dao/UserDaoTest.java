package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userDao = new UserDao(jdbcTemplate);
    }

    @AfterEach
    void tearDown() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());

        jdbcTemplate.execute("truncate table users restart identity");
    }

    @Test
    void findAll() {
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);

        List<User> users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);

        User findUser = userDao.findById(1L);

        assertThat(findUser.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        String account = "gugu";
        User user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        User findUser = userDao.findByAccount(account);

        assertThat(findUser.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        String account = "insert-gugu";
        User user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        User insertUser = userDao.findByAccount(account);

        assertThat(insertUser.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
        String newPassword = "password99";
        User findUser = userDao.findByAccount("gugu");

        findUser.changePassword(newPassword);
        userDao.update(findUser);

        User actual = userDao.findByAccount("gugu");

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
