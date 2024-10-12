package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementResolver;
import com.interface21.jdbc.datasource.DataAccessWrapper;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private UserDao userDao;
    private JdbcTemplate jdbcTemplate;
    private Connection connection;

    @BeforeEach
    void setup() throws SQLException {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        this.connection = DataSourceConfig.getInstance().getConnection();
        jdbcTemplate = new JdbcTemplate(new DataAccessWrapper(), new PreparedStatementResolver());
        userDao = new UserDao(jdbcTemplate);

        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(connection, user);
    }

    @AfterEach
    void tearDown() throws SQLException {
        jdbcTemplate.queryForUpdate(connection, "DELETE FROM users");
        jdbcTemplate.queryForUpdate(connection, "ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    void findAll() {
        final var users = userDao.findAll(connection);

        assertThat(users).isNotEmpty();
    }

    @DisplayName("아이디 값을 통해 유저를 찾을 수 있다")
    @Test
    void findById() {
        final var user = userDao.findById(connection, 1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @DisplayName("계좌를 통해 유저를 찾을 수 있다")
    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(connection, account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(connection, user);

        final var actual = userDao.findById(connection,2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @DisplayName("유저 테이블의 컬럼을 전체 업데이트할 수 있다")
    @Test
    void update() throws SQLException {
        final var newPassword = "password99";
        final var user = userDao.findById(connection, 1L);
        user.changePassword(newPassword);

        userDao.update(connection, user);

        final var actual = userDao.findById(connection, 1L);
        connection.commit();

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
