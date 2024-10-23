package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;
    private DataSource dataSource;

    @BeforeEach
    void setup() {
        dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.write("TRUNCATE TABLE users RESTART IDENTITY");
        userDao = new UserDao(jdbcTemplate);
    }

    @Test
    void findAll() {
        userDao.save(new User("gugu", "password", "hkkang@woowahan.com"));
        List<User> users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        userDao.save(new User("gugu", "password", "hkkang@woowahan.com"));
        User user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        String account = "gugu";
        userDao.save(new User(account, "password", "hkkang@woowahan.com"));
        User user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        String account = "insert-gugu";
        userDao.save(new User(account, "password", "hkkang@woowahan.com"));

        User actual = userDao.findById(1L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        String newPassword = "password99";
        userDao.save(new User("gugu", "password", "hkkang@woowahan.com"));
        User user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        User actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
