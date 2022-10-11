package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;
    private Connection connection;

    @BeforeEach
    void setup() throws SQLException {
        DataSource dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        connection = dataSource.getConnection();
        userDao = new UserDao(new JdbcTemplate(dataSource));
        final User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user, connection);
    }

    @Test
    void findAll() {
        final List<User> users = userDao.findAll(connection);

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final User user = userDao.findById(1L, connection);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Disabled
    @Test
    void findByAccount() {
        final String account = "gugu";
        final User user = userDao.findByAccount(account, connection);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final String account = "insert-gugu";
        final User user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user, connection);

        final User actual = userDao.findById(2L, connection);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final String newPassword = "password99";
        final User user = userDao.findById(1L, connection);
        user.changePassword(newPassword);

        userDao.update(user, connection);

        final User actual = userDao.findById(1L, connection);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
