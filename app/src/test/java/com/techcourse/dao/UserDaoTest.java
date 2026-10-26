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
    @DisplayName("일반 수정은 모든 값을 올바른 컬럼에 저장하고 대상 사용자만 변경한다")
    void update() {
        userDao.insert(new User("other", "otherPassword", "other@example.com"));
        final var user = new User(1L, "changed-account", "password99", "changed@example.com");

        userDao.update(user);

        final var actual = userDao.findById(1L);

        assertThat(actual).usingRecursiveComparison().isEqualTo(user);
        assertThat(userDao.findById(2L).getPassword()).isEqualTo("otherPassword");
    }

    @Test
    @DisplayName("외부 연결의 수정은 모든 값을 바인딩하고 호출자가 롤백하거나 커밋할 때까지 연결을 유지한다")
    void updateWithCallerConnectionLeavesTransactionCompletionToTheCaller() throws SQLException {
        final var original = userDao.findById(1L);
        final var changed = new User(1L, "changed-account", "password99", "changed@example.com");
        databaseAnchor.setAutoCommit(false);

        userDao.update(databaseAnchor, changed);

        assertThat(databaseAnchor.isClosed()).isFalse();
        assertThat(userDao.findById(databaseAnchor, 1L)).usingRecursiveComparison().isEqualTo(changed);
        assertThat(userDao.findById(1L)).usingRecursiveComparison().isEqualTo(original);

        databaseAnchor.rollback();

        assertThat(userDao.findById(1L)).usingRecursiveComparison().isEqualTo(original);
        userDao.update(databaseAnchor, changed);
        databaseAnchor.commit();
        assertThat(userDao.findById(1L)).usingRecursiveComparison().isEqualTo(changed);
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
