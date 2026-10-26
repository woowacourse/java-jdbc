package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class UserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new UserService(userDao, userHistoryDao, DataSourceConfig.getInstance());

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var userService = new UserService(userDao, userHistoryDao, DataSourceConfig.getInstance());

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }

    @Test
    void rollsBackPasswordChangeWhenHistoryInsertFailsInDatabase() throws SQLException {
        final var originalUser = userDao.findById(1L);
        final var originalPassword = originalUser.getPassword();
        final var historyCountSql = "select count(*) from user_history where user_id = ?";
        final long originalHistoryCount = jdbcTemplate.queryForObject(
                historyCountSql, resultSet -> resultSet.getLong(1), originalUser.getId());

        try (final Connection connection = spy(DataSourceConfig.getInstance().getConnection())) {
            final DataSource transactionDataSource = mock(DataSource.class);
            given(transactionDataSource.getConnection()).willReturn(connection);
            final var userService = new UserService(userDao, new UserHistoryDao(jdbcTemplate), transactionDataSource);

            // created_by의 NOT NULL 제약조건을 위반해 실제 이력 INSERT에서 SQLException이 발생한다.
            assertThatThrownBy(() -> userService.changePassword(originalUser.getId(), "newPassword", null))
                    .isInstanceOf(DataAccessException.class)
                    .hasRootCauseInstanceOf(SQLIntegrityConstraintViolationException.class);

            assertThat(userDao.findById(originalUser.getId()).getPassword()).isEqualTo(originalPassword);
            final long actualHistoryCount = jdbcTemplate.queryForObject(
                    historyCountSql, resultSet -> resultSet.getLong(1), originalUser.getId());
            assertThat(actualHistoryCount).isEqualTo(originalHistoryCount);

            // H2의 close 시 자동 롤백만으로 통과하지 않도록 서비스의 명시적인 롤백도 확인한다.
            final var inOrder = inOrder(connection);
            inOrder.verify(connection).rollback();
            inOrder.verify(connection).close();
            verify(connection, never()).commit();
        }
    }

    @Test
    void rollsBackWhenUserUpdateFailsInDatabase() throws SQLException {
        final var originalUser = userDao.findById(1L);
        final var originalPassword = originalUser.getPassword();
        final var userHistoryDao = mock(UserHistoryDao.class);

        try (final Connection connection = spy(DataSourceConfig.getInstance().getConnection())) {
            final DataSource transactionDataSource = mock(DataSource.class);
            given(transactionDataSource.getConnection()).willReturn(connection);
            final var userService = new UserService(userDao, userHistoryDao, transactionDataSource);

            // password의 NOT NULL 제약조건을 위반해 실제 UserDao의 UPDATE에서 예외가 발생한다.
            assertThatThrownBy(() -> userService.changePassword(originalUser.getId(), null, "gugu"))
                    .isInstanceOf(DataAccessException.class)
                    .hasRootCauseInstanceOf(SQLIntegrityConstraintViolationException.class);

            assertThat(userDao.findById(originalUser.getId()).getPassword()).isEqualTo(originalPassword);
            verifyNoInteractions(userHistoryDao);

            final var inOrder = inOrder(connection);
            inOrder.verify(connection).rollback();
            inOrder.verify(connection).close();
            verify(connection, never()).commit();
        }
    }
}
