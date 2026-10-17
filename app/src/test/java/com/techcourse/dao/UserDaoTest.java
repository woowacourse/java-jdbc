package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private UserDao userDao;

    private void clearUsers() throws SQLException {
        try (var connection = DataSourceConfig.getInstance().getConnection();
             var statement = connection.createStatement()) {
            statement.execute("TRUNCATE TABLE users RESTART IDENTITY");
        }
    }

    @BeforeEach
    void setup() throws SQLException {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao(new JdbcTemplate(DataSourceConfig.getInstance()));

        clearUsers();

        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    @DisplayName("저장된 사용자가 있으면 전체 조회 결과가 비어 있지 않다")
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    @DisplayName("전체 조회는 저장된 모든 사용자를 반환한다")
    void findAllReturnsEveryUser() {
        userDao.insert(new User("isaac", "password2", "isaac@example.com"));

        final var users = userDao.findAll();

        assertThat(users).extracting(User::getAccount)
                .containsExactlyInAnyOrder("gugu", "isaac");
    }

    @Test
    @DisplayName("저장된 사용자가 없으면 전체 조회는 빈 목록을 반환한다")
    void findAllReturnsEmptyListWhenNoUsersExist() throws SQLException {
        clearUsers();

        assertThat(userDao.findAll()).isEmpty();
    }

    @Test
    @DisplayName("ID로 해당 사용자를 조회한다")
    void findById() {
        final var user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    @DisplayName("계정으로 해당 사용자를 조회한다")
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    @DisplayName("사용자를 저장하면 ID로 다시 조회할 수 있다")
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    @DisplayName("사용자의 비밀번호를 수정하면 변경된 값이 저장된다")
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
