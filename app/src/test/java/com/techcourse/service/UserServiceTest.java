package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private UserDao userDao;

    @BeforeEach
    void setUp() throws SQLException {
        this.userDao = new UserDao(DataSourceConfig.getInstance());

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        try (final var connection = DataSourceConfig.getInstance().getConnection();
             final var statement = connection.createStatement()) {
            statement.executeUpdate("truncate table user_history restart identity");
            statement.executeUpdate("truncate table users restart identity");
        }
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() throws SQLException {
        final var userHistoryDao = new UserHistoryDao(DataSourceConfig.getInstance());
        final var userService = new UserService(userDao, userHistoryDao, DataSourceConfig.getInstance());

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
        assertHistoryCount(1);
        try (final var connection = DataSourceConfig.getInstance().getConnection();
             final var statement = connection.prepareStatement("select * from user_history where user_id = ?")) {
            statement.setLong(1, 1L);
            try (final var resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getString("account")).isEqualTo("gugu");
                assertThat(resultSet.getString("password")).isEqualTo(newPassword);
                assertThat(resultSet.getString("email")).isEqualTo("hkkang@woowahan.com");
                assertThat(resultSet.getString("created_by")).isEqualTo(createBy);
                assertThat(resultSet.getTimestamp("created_at")).isNotNull();
            }
        }
    }

    @Test
    void testTransactionRollback() throws SQLException {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(DataSourceConfig.getInstance());
        final var userService = new UserService(userDao, userHistoryDao, DataSourceConfig.getInstance());

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo("password");
        assertHistoryCount(0);
    }

    @Test
    void rollbackAfterHistoryWasInserted() throws SQLException {
        final var userHistoryDao = new UserHistoryDao(DataSourceConfig.getInstance()) {
            @Override
            public void log(final Connection connection, final UserHistory history) {
                super.log(connection, history);
                throw new DataAccessException("이력 저장 후 실패");
            }
        };
        final var service = new UserService(userDao, userHistoryDao, DataSourceConfig.getInstance());

        assertThrows(DataAccessException.class,
                () -> service.changePassword(1L, "newPassword", "gugu"));

        assertThat(userDao.findById(1L).getPassword()).isEqualTo("password");
        assertHistoryCount(0);
    }

    private void assertHistoryCount(final int expected) throws SQLException {
        try (final var connection = DataSourceConfig.getInstance().getConnection();
             final var statement = connection.prepareStatement("select count(*) from user_history");
             final var resultSet = statement.executeQuery()) {
            resultSet.next();
            assertThat(resultSet.getInt(1)).isEqualTo(expected);
        }
    }
}
