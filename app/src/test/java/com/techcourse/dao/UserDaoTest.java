package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DataSource dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        userDao = new UserDao(jdbcTemplate);

        jdbcTemplate.update("truncate table users");
        jdbcTemplate.update("alter table users alter column id restart with 1");
    }

    @Test
    void findAll() {
        // given
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));

        // when
        List<User> users = userDao.findAll();

        // then
        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        // given
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));

        // when
        User user = userDao.findById(1L);

        // then
        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        // given
        String account = "gugu";
        userDao.insert(new User(account, "password", "hkkang@woowahan.com"));

        // when
        User user = userDao.findByAccount(account);

        // then
        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        // given
        String account = "insert-gugu";
        User user = new User(account, "password", "hkkang@woowahan.com");

        // when
        userDao.insert(user);

        // then
        User actual = userDao.findById(1L);
        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        // given
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));

        String newPassword = "password99";
        User user = userDao.findById(1L);
        user.changePassword(newPassword);

        // when
        userDao.update(user);

        // then
        User actual = userDao.findById(1L);
        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
