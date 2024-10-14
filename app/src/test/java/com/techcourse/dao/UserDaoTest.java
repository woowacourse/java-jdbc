package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() throws SQLException {
        DataSource dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);

        dataSource.getConnection();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        userDao = new UserDao(jdbcTemplate);

        setDefaultData(jdbcTemplate);
    }

    private void setDefaultData(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("truncate table users restart identity");
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final var user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
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
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void insertWithPss() {
        String account = "jazz";
        User user = new User(account, "password", "jazz@woowahan.com");
        userDao.insertWithPss(user);

        User actual = userDao.findById(2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void findAllByEmailWithPss() {
        String email = "jazz@woowahan.com";
        User userJazz = new User("jazz", "password", email);
        User userDodo = new User("dodo", "password", email);
        userDao.insertWithPss(userJazz);
        userDao.insertWithPss(userDodo);

        List<User> users = userDao.findAllByEmailWithPss(email);

        assertAll(
                () -> assertThat(users.size()).isEqualTo(2),
                () -> assertThat(users.getFirst().getEmail()).isEqualTo(email),
                () -> assertThat(users.getLast().getEmail()).isEqualTo(email)
        );
    }
}
