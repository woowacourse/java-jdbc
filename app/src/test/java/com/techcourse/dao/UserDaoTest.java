package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() throws SQLException {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        clearUsers();

        userDao = new UserDao(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    @DisplayName("전체 조회는 저장된 모든 사용자의 정보를 반환한다")
    void findAll() {
        userDao.insert(new User("other", "otherPassword", "other@example.com"));

        final var users = userDao.findAll();

        assertThat(users)
                .extracting(User::getId, User::getAccount, User::getPassword, User::getEmail)
                .containsExactlyInAnyOrder(
                        tuple(1L, "gugu", "password", "hkkang@woowahan.com"),
                        tuple(2L, "other", "otherPassword", "other@example.com"));
    }

    @Test
    @DisplayName("ID로 조회하면 해당 사용자의 모든 정보를 반환한다")
    void findById() {
        final var user = userDao.findById(1L);

        assertThat(user).usingRecursiveComparison()
                .isEqualTo(new User(1L, "gugu", "password", "hkkang@woowahan.com"));
    }

    @Test
    @DisplayName("계정으로 조회하면 해당 사용자의 모든 정보를 반환한다")
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user).usingRecursiveComparison()
                .isEqualTo(new User(1L, "gugu", "password", "hkkang@woowahan.com"));
    }

    @Test
    @DisplayName("등록한 사용자의 모든 정보를 다시 조회할 수 있다")
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(2L);

        assertThat(actual).usingRecursiveComparison()
                .isEqualTo(new User(2L, "insert-gugu", "password", "hkkang@woowahan.com"));
    }

    @Test
    @DisplayName("비밀번호 변경을 저장하면 대상 사용자에게만 반영된다")
    void update() {
        userDao.insert(new User("other", "otherPassword", "other@example.com"));
        final var newPassword = "password99";
        final var user = userDao.findById(2L);
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(2L);

        assertThat(actual).usingRecursiveComparison()
                .isEqualTo(new User(2L, "other", "password99", "other@example.com"));
        assertThat(userDao.findById(1L).getPassword()).isEqualTo("password");
    }

    @Test
    @DisplayName("저장된 사용자가 없으면 빈 목록을 반환한다")
    void findAllWhenEmpty() throws SQLException {
        clearUsers();

        assertThat(userDao.findAll()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회하면 null을 반환한다")
    void findByMissingId() {
        assertThat(userDao.findById(999L)).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 계정으로 조회하면 null을 반환한다")
    void findByMissingAccount() {
        assertThat(userDao.findByAccount("missing")).isNull();
    }

    @Test
    @DisplayName("작은따옴표가 포함된 계정도 그대로 저장하고 조회한다")
    void findAccountContainingQuote() {
        userDao.insert(new User("o'connor", "quotedPassword", "quoted@example.com"));

        final var user = userDao.findByAccount("o'connor");

        assertThat(user).usingRecursiveComparison()
                .isEqualTo(new User(2L, "o'connor", "quotedPassword", "quoted@example.com"));
    }

    private void clearUsers() throws SQLException {
        try (final var connection = DataSourceConfig.getInstance().getConnection();
             final var statement = connection.createStatement()) {
            statement.execute("truncate table users restart identity");
        }
    }
}
