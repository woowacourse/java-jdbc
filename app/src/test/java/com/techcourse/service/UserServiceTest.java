package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private Connection keepAliveConnection;

    @BeforeEach
    void setUp() throws SQLException {
        final var testDataSource = new JdbcDataSource();
        testDataSource.setURL("jdbc:h2:mem:user-service-test-" + UUID.randomUUID());
        this.dataSource = testDataSource;
        // DAO가 커넥션을 닫아도 테스트가 끝날 때까지 메모리 DB를 유지한다.
        this.keepAliveConnection = dataSource.getConnection();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new UserDao(dataSource);

        DatabasePopulatorUtils.execute(dataSource);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (keepAliveConnection != null) {
            keepAliveConnection.close();
        }
    }

    @Test
    void changePassword_Success_CommitsPasswordAndHistory() throws SQLException {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new UserService(userDao, userHistoryDao, dataSource);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);

        // 별도 커넥션에서 조회해 두 작업이 실제 커밋되었는지 확인한다.
        final var sql = "select user_id, account, password, email, created_at, created_by from user_history where user_id = ?";
        try (Connection connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, 1L);
            try (var resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getLong("user_id")).isEqualTo(1L);
                assertThat(resultSet.getString("account")).isEqualTo("gugu");
                assertThat(resultSet.getString("password")).isEqualTo(newPassword);
                assertThat(resultSet.getString("email")).isEqualTo("hkkang@woowahan.com");
                assertThat(resultSet.getTimestamp("created_at")).isNotNull();
                assertThat(resultSet.getString("created_by")).isEqualTo(createBy);
                assertThat(resultSet.next()).isFalse();
            }
        }
    }

    @Test
    void changePassword_HistorySaveFails_PreservesOriginalPassword() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var userService = new UserService(userDao, userHistoryDao, dataSource);

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        final var originalPassword = userDao.findById(1L).getPassword();
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(originalPassword);
        assertThat(historyCount(1L)).isZero();
    }

    @Test
    void changePassword_FailureAfterHistoryInsert_RollsBackPasswordAndHistory() {
        final var failure = new DataAccessException("Failure after history insert");
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate) {
            @Override
            public void log(UserHistory userHistory, Connection connection) {
                super.log(userHistory, connection);
                throw failure;
            }
        };
        final var userService = new UserService(userDao, userHistoryDao, dataSource);
        final var originalPassword = userDao.findById(1L).getPassword();

        final var actualException = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(actualException).isSameAs(failure);
        assertThat(userDao.findById(1L).getPassword()).isEqualTo(originalPassword);
        assertThat(historyCount(1L)).isZero();
    }

    private long historyCount(long userId) {
        return jdbcTemplate.query("select count(*) from user_history where user_id = ?",
                rs -> rs.getLong(1), userId).getFirst();
    }
}
