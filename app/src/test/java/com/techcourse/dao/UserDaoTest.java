package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.PreparedStatement;
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
        clearTables(jdbcTemplate);
        userDao = new UserDao(jdbcTemplate);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    private static void clearTables(final JdbcTemplate jdbcTemplate) {
        jdbcTemplate.executeQuery((connection, sql) -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
        }, "truncate table users");

        jdbcTemplate.executeQuery((connection, sql) -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
        }, "alter table users alter column id restart with 1");
    }

    @Test
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users).hasSize(1);
    }

    @Test
    void findById() {
        final var user = userDao.findById(1L);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getAccount()).isEqualTo("gugu");
        assertThat(user.getEmail()).isEqualTo("hkkang@woowahan.com");
        assertThat(user.getPassword()).isEqualTo("password");
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getAccount()).isEqualTo("gugu");
        assertThat(user.getEmail()).isEqualTo("hkkang@woowahan.com");
        assertThat(user.getPassword()).isEqualTo("password");
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(2L);

        assertThat(actual.getId()).isEqualTo(2L);
        assertThat(actual.getAccount()).isEqualTo("insert-gugu");
        assertThat(actual.getEmail()).isEqualTo("hkkang@woowahan.com");
        assertThat(actual.getPassword()).isEqualTo("password");
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L);

        assertThat(actual.getId()).isEqualTo(1L);
        assertThat(actual.getAccount()).isEqualTo("gugu");
        assertThat(actual.getEmail()).isEqualTo("hkkang@woowahan.com");
        assertThat(actual.getPassword()).isEqualTo("password99");
    }
}
