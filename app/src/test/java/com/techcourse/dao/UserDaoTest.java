package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        truncateUsers(DataSourceConfig.getInstance());

        userDao = new UserDao(new JdbcTemplate(DataSourceConfig.getInstance()));
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
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
    void update() throws SQLException {
        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);

        try (final var connection = DataSourceConfig.getInstance().getConnection()) {
            userDao.update(connection, user);
        }

        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    // 넘겨받은 Connection으로 실행해야 호출한 쪽의 트랜잭션에 묶인다. 롤백하면 변경이 남지 않아야 한다.
    @Test
    void updateParticipatesInCallerTransaction() throws SQLException {
        final var originalPassword = userDao.findById(1L).getPassword();
        final var user = userDao.findById(1L);
        user.changePassword("password99");

        try (final var connection = DataSourceConfig.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            userDao.update(connection, user);
            connection.rollback();
        }

        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(originalPassword);
    }

    // 인메모리 DB가 테스트 간에 공유되므로, 매 테스트마다 행을 비우고 id를 1부터 다시 시작시킨다.
    private void truncateUsers(final DataSource dataSource) {
        try (final var connection = dataSource.getConnection();
             final var statement = connection.createStatement()) {
            statement.execute("truncate table users restart identity");
        } catch (SQLException e) {
            throw new IllegalStateException("users 테이블 초기화에 실패했습니다.", e);
        }
    }
}
