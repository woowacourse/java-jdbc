package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() throws SQLException {
        assertThat(((org.h2.jdbcx.JdbcDataSource) DataSourceConfig.getInstance()).getURL())
                .as("초기화는 테스트 메모리 DB에서만 허용합니다. jdbc.url 설정을 확인하세요.")
                .startsWith("jdbc:h2:mem:");
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        // 스키마 생성만으로는 기존 행이 지워지지 않으므로 테스트마다 초기화한다.
        try (var connection = DataSourceConfig.getInstance().getConnection();
             var statement = connection.createStatement()) {
            statement.execute("truncate table users restart identity");
        }

        userDao = new UserDao(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findAllContainsEveryUser() {
        userDao.insert(new User("other", "secret", "other@example.com"));

        assertThat(userDao.findAll()).extracting(User::getAccount)
                .containsExactlyInAnyOrder("gugu", "other");
    }

    @Test
    void findById() {
        final var user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getPassword()).isEqualTo("password");
        assertThat(user.getEmail()).isEqualTo("hkkang@woowahan.com");
    }

    @Test
    void findByIdReturnsNullWhenMissing() {
        assertThat(userDao.findById(999L)).isNull();
    }

    @Test
    void findAllReturnsEmptyListWhenNoUsers() throws SQLException {
        try (var connection = DataSourceConfig.getInstance().getConnection();
             var statement = connection.createStatement()) {
            statement.executeUpdate("delete from users");
        }

        assertThat(userDao.findAll()).isEmpty();
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getPassword()).isEqualTo("password");
        assertThat(user.getEmail()).isEqualTo("hkkang@woowahan.com");
    }

    @Test
    void findByAccountReturnsNullWhenMissing() {
        assertThat(userDao.findByAccount("missing")).isNull();
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(2L);

        assertThat(actual.getAccount()).isEqualTo(account);
        assertThat(actual.getPassword()).isEqualTo(user.getPassword());
        assertThat(actual.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
        assertThat(actual.getAccount()).isEqualTo("gugu");
        assertThat(actual.getEmail()).isEqualTo("hkkang@woowahan.com");
    }

    @Test
    void updateDoesNotChangeAnotherUser() {
        userDao.insert(new User("other", "secret", "other@example.com"));
        final var user = userDao.findById(1L);
        user.changePassword("changed");

        userDao.update(user);

        assertThat(userDao.findByAccount("other").getPassword()).isEqualTo("secret");
    }

    @Test
    void acceptsJdbcTemplateForWritesAndQueries() {
        final var dao = new UserDao(new JdbcTemplate(DataSourceConfig.getInstance()));
        dao.insert(new User("template-user", "secret", "template@example.com"));
        final var user = dao.findByAccount("template-user");
        user.changePassword("changed");
        dao.update(user);

        assertThat(dao.findAll()).hasSize(2);
        assertThat(dao.findById(user.getId()).getPassword()).isEqualTo("changed");
    }
}
