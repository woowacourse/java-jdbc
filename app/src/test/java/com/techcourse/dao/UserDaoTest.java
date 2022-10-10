package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.DatabaseCleanUp;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import nextstep.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userDao = new UserDao(jdbcTemplate);

        final var user = new User(1L, "gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final User user = userDao.findByAccount("gugu");
        final var foundUser = userDao.findById(user.getId());

        assertThat(foundUser).usingRecursiveComparison()
                .isEqualTo(user);
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findByAccount("gugu");
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findByAccount("gugu");

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @AfterEach
    void setDown() {
        DatabaseCleanUp.cleanUp(jdbcTemplate, "users");
        DatabaseCleanUp.cleanUp(jdbcTemplate, "user_history");
    }
}
