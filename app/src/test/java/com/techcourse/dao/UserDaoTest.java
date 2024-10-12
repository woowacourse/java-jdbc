package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;
    private Connection connection;

    @BeforeEach
    void setup() throws SQLException {
        DataSource dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        connection = dataSource.getConnection();

        userDao = new UserDao();
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(connection, user);
    }

    @AfterEach
    void tearDown() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.executeUpdate(connection,
                """
                        delete from users;
                        alter table users alter column id restart with 1;
                        """);
    }

    @DisplayName("전체 조회")
    @Test
    void findAll() {
        final var users = userDao.findAll(connection);

        assertThat(users).isNotEmpty();
    }

    @DisplayName("존재하는 id로 단건 조회 -> present")
    @Test
    void findById() {
        final var user = userDao.findById(connection, 1L);

        assertThat(user.get().getAccount()).isEqualTo("gugu");
    }

    @DisplayName("존재하지 않는 id로 단건 조회 -> empty")
    @Test
    void findById_InvalidUser() {
        assertThat(userDao.findById(connection, 2L)).isEmpty();
    }

    @DisplayName("존재하는 account로 단건 조회 -> present")
    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(connection, account);

        assertThat(user.get().getAccount()).isEqualTo(account);
    }

    @DisplayName("존재하지 않는 account로 단건 조회 -> empty")
    @Test
    void findByAccount_InvalidAccount() {
        assertThat(userDao.findByAccount(connection, "not-exist")).isEmpty();
    }

    @DisplayName("유저 저장")
    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(connection, user);

        final var actual = userDao.findById(connection, 2L).get();

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @DisplayName("유저 정보 update")
    @Test
    void update() throws SQLException {
        final var newPassword = "password99";
        final var user = userDao.findById(connection, 1L).get();
        user.changePassword(newPassword);

        userDao.update(connection, user);

        final var actual = userDao.findById(connection, 1L).get();

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
