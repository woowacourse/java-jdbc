package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.exception.EmptyResultException;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userDao = new UserDao(jdbcTemplate);
    }

    @AfterEach
    void tearDown() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());

        jdbcTemplate.execute("truncate table users restart identity");
    }

    @Test
    void findAll() throws SQLException {
        Connection connection = DataSourceConfig.getInstance().getConnection();
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(connection, user);

        List<User> users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() throws SQLException {
        Connection connection = DataSourceConfig.getInstance().getConnection();
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(connection, user);

        User findUser = userDao.findById(1L);

        assertThat(findUser.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByIdFailWhenResultIsEmpty() {
        assertThatThrownBy(() -> userDao.findById(-1L))
                .isInstanceOf(EmptyResultException.class)
                .hasMessage("일치하는 결과가 존재하지 않습니다.");
    }

    @Test
    void findByAccount() throws SQLException {
        Connection connection = DataSourceConfig.getInstance().getConnection();
        String account = "gugu";
        User user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(connection, user);

        User findUser = userDao.findByAccount(account);

        assertThat(findUser.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() throws SQLException {
        Connection connection = DataSourceConfig.getInstance().getConnection();
        String account = "insert-gugu";
        User user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(connection, user);

        User insertUser = userDao.findByAccount(account);

        assertThat(insertUser.getAccount()).isEqualTo(account);
    }

    @Test
    void update() throws SQLException {
        Connection connectionA = DataSourceConfig.getInstance().getConnection();
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(connectionA, user);
        String newPassword = "password99";
        User findUser = userDao.findByAccount("gugu");

        Connection connectionB = DataSourceConfig.getInstance().getConnection();
        findUser.changePassword(newPassword);
        userDao.update(connectionB, findUser);

        User actual = userDao.findByAccount("gugu");

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
