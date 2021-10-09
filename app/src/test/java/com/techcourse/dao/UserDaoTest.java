package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import nextstep.datasource.DatabasePopulator;
import nextstep.jdbc.JdbcTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.net.URL;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private UserDao userDao;
    private Long dummyId;

    @BeforeEach
    void setup() {
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        DataSource dataSource = dataSourceConfig.dataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        userDao = new UserDao(jdbcTemplate);
        URL url = getClass().getClassLoader().getResource("schema.sql");
        DatabasePopulator databasePopulator = new DatabasePopulator(dataSource);
        databasePopulator.execute(url);

        final User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
        final User createdUser = userDao.findByAccount("gugu").orElseThrow(IllegalStateException::new);
        dummyId = createdUser.getId();
    }

    @AfterEach
    void tearDown() {
        userDao.deleteById(dummyId);
    }

    @Test
    void insert() {
        final String account = "insert-gugu";
        final User user = new User(account, "password", "hkkang@woowahan.com");

        int affectedCount = userDao.insert(user);

        assertThat(affectedCount).isEqualTo(1);
        assertThat(userDao.findByAccount(account))
                .get()
                .extracting(User::getAccount)
                .isEqualTo(account);
    }

    @Test
    void findById() {
        assertThat(userDao.findById(dummyId))
                .get()
                .extracting(User::getAccount)
                .isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final String account = "gugu";

        assertThat(userDao.findByAccount(account))
                .get()
                .extracting(User::getId)
                .isEqualTo(dummyId);
    }

    @Test
    void findAll() {
        final List<User> users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void update() {
        final String newPassword = "password99";
        final User user = userDao.findById(dummyId).orElseThrow(IllegalStateException::new);
        user.changePassword(newPassword);

        userDao.update(user);

        final User actual = userDao.findById(dummyId).orElseThrow(IllegalStateException::new);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void deleteById() {
        final int affectedResult = userDao.deleteById(dummyId);

        assertThat(affectedResult).isEqualTo(1);
    }
}
