package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;
    private DataSource dataSource;
    private Long guguId;

    @BeforeEach
    void setup() {
        dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);

        userDao = new UserDao(new JdbcTemplate(dataSource));
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        guguId = userDao.insert(user);
    }

    @AfterEach
    void teardown() throws SQLException {
        final Connection connection = dataSource.getConnection();

        final String sql_truncate = "truncate table users";
        final PreparedStatement statement_truncate = connection.prepareStatement(sql_truncate);
        statement_truncate.execute();

        final String sql_alter = "alter table users alter column id restart with 1";
        final PreparedStatement statement_alter = connection.prepareStatement(sql_alter);
        statement_alter.execute();
    }

    @Test
    void findAll() {
        final var user = new User("yaho", "password", "yaho@email.com");
        userDao.insert(user);

        final var users = userDao.findAll();

        assertThat(users.size()).isEqualTo(2);
    }

    @Test
    void findById() {
        final var user = userDao.findById(guguId);

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
        Long id = userDao.insert(user);

        final var actual = userDao.findById(id);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(guguId);
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(guguId);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
