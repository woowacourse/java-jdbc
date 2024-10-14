package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private DataSource dataSource;
    private UserDao userDao;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        jdbcTemplate = new JdbcTemplate(dataSource);
        userDao = new UserDao(jdbcTemplate);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM users;");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN id RESTART WITH 1;");
    }

    @DisplayName("모든 유저 정보를 조회한다.")
    @Test
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @DisplayName("아이디로 유저 정보를 조회한다.")
    @Test
    void findById() {
        final var user = userDao.findById(1L);

        assertAll(
                () -> assertThat(user).isPresent(),
                () -> assertThat(user.get().account()).isEqualTo("gugu")
        );
    }

    @DisplayName("아이디로 유저 정보를 조회시 데이터가 없다면 비어있다.")
    @Test
    void findByIdEmpty() {
        final var user = userDao.findById(2L);

        assertThat(user).isEmpty();
    }

    @DisplayName("계정으로 유저 정보를 조회한다.")
    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertAll(
                () -> assertThat(user).isPresent(),
                () -> assertThat(user.get().account()).isEqualTo("gugu")
        );
    }

    @DisplayName("계정으로 유저 정보를 조회시 데이터가 없다면 비어있다.")
    @Test
    void findByAccountEmpty() {
        final var account = "gumayushi";
        final var user = userDao.findByAccount(account);

        assertThat(user).isEmpty();
    }

    @DisplayName("유저를 추가한다.")
    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(2L)
                .orElseThrow();

        assertThat(actual.account()).isEqualTo(account);
    }

    @DisplayName("유저 정보를 업데이트 한다.")
    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L)
                .orElseThrow();

        User newUser = user.changePassword(newPassword);

        userDao.update(newUser);
        final var actual = userDao.findById(1L)
                .orElseThrow();

        assertThat(actual.password()).isEqualTo(newPassword);
    }
}
