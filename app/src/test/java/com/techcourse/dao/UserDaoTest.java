package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.List;
import java.util.Optional;
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
        Optional<User> user = userDao.findById(1L);

        // then
        assertThat(user).isNotEmpty();
    }

    @Test
    void findByAccount() {
        // given
        String account = "gugu";
        userDao.insert(new User(account, "password", "hkkang@woowahan.com"));

        // when
        Optional<User> user = userDao.findByAccount(account);

        // then
        assertThat(user).isNotEmpty();
    }

    @Test
    void insert() {
        // given
        String account = "insert-gugu";
        User user = new User(account, "password", "hkkang@woowahan.com");

        // when
        userDao.insert(user);

        // then
        Optional<User> actual = userDao.findById(1L);
        assertThat(actual).isNotEmpty();
    }

    @Test
    void update() {
        // given
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));

        String newPassword = "password99";
        User user = getUser(1L);
        user.changePassword(newPassword);

        // when
        userDao.update(user);

        // then
        User actual = getUser(1L);
        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    private User getUser(final Long id) {
        return userDao.findById(id)
                .get();
    }
}
