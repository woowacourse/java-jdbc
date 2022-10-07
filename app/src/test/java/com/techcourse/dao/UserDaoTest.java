package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.util.List;
import nextstep.jdbc.DatabasePopulatorUtils;
import nextstep.jdbc.JdbcTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;
    private JdbcTemplate jdbcTemplate;

    private User gugu;
    private User josh;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userDao = new UserDao(DataSourceConfig.getInstance());
        gugu = new User("gugu", "password", "hkkang@woowahan.com");
        josh = new User("josh", "password", "whgusrms96@gmail.com");

        userDao.insert(gugu);
        userDao.insert(josh);
    }

    @AfterEach
    void refresh() {
        final String sql = "DELETE FROM users";
        jdbcTemplate.deleteAll(sql);
    }

    @Test
    void findAll() {
        final List<User> users = userDao.findAll();

        assertAll(
                () -> assertThat(users.size()).isEqualTo(2),
                () -> assertThat(users).contains(gugu),
                () -> assertThat(users).contains(josh)
        );
    }

    @Test
    void findById() {
        final User user = userDao.findById(gugu.getId());

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final String account = "gugu";
        final User user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final String account = "insert-gugu";
        final User user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final User actual = userDao.findById(user.getId());

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final String newPassword = "password99";
        final User user = userDao.findById(gugu.getId());
        user.changePassword(newPassword);

        userDao.update(user);

        final User actual = userDao.findById(gugu.getId());

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
