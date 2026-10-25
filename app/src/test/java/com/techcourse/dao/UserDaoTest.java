package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserDaoTest {

    private UserDao userDao;
    private Connection databaseAnchor;

    @BeforeEach
    void setup() throws SQLException {
        final var dataSource = DataSourceConfig.createDataSource(
                "jdbc:h2:mem:user-dao-" + UUID.randomUUID());
        databaseAnchor = dataSource.getConnection();
        DatabasePopulatorUtils.execute(dataSource);

        userDao = new UserDao(new JdbcTemplate(dataSource));
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        databaseAnchor.close();
    }

    @Test
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final var user = userDao.findById(1L);

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
        userDao.insert(user);

        final var actual = userDao.findById(2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    @DisplayName("동일 계정이 두 행이면 임의의 사용자를 선택하지 않고 다건 조회 예외를 던진다")
    void duplicateAccountsFailSingleUserLookup() {
        userDao.insert(new User("gugu", "anotherPassword", "other@example.com"));

        final var failure = assertThrows(DataAccessException.class, () -> userDao.findByAccount("gugu"));

        assertThat(failure).hasMessage("Expected at most one row, but found two or more").hasNoCause();
        assertThat(userDao.findAll()).hasSize(2);
    }
}
