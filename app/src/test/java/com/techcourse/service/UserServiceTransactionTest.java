package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

// 실제 H2는 커밋하지 않은 Connection을 닫을 때 알아서 롤백해 준다.
// 그 동작은 JDBC 표준이 아니라 드라이버마다 다르므로, 여기서는 mock Connection으로 서비스가 직접 커밋, 롤백, 닫기를 하는지 확인한다.
class UserServiceTransactionTest {

    private static final long USER_ID = 1L;
    private static final String NEW_PASSWORD = "newPassword";
    private static final String CREATE_BY = "gugu";

    private DataSource dataSource;
    private Connection connection;
    private UserDao userDao;

    // 예외 자신과, cause 및 suppressed를 따라가며 닿는 모든 예외를 모은다.
    private static List<Throwable> reachableThrowables(final Throwable root) {
        final List<Throwable> found = new ArrayList<>();
        collect(root, found);
        return found;
    }

    private static void collect(final Throwable throwable, final List<Throwable> found) {
        if (throwable == null || found.contains(throwable)) {
            return;
        }
        found.add(throwable);
        collect(throwable.getCause(), found);
        for (final Throwable suppressed : throwable.getSuppressed()) {
            collect(suppressed, found);
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(anyString())).willReturn(preparedStatement);
        given(preparedStatement.executeUpdate()).willReturn(1);

        // DAO가 서비스의 Connection을 쓰지 않고 스스로 Connection을 얻으려 하면 이 DataSource를 거치게 된다.
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(mock(DataSource.class));
        userDao = new UserDao(jdbcTemplate) {
            @Override
            public User findById(final Long id) {
                return new User(id, "gugu", "password", "hkkang@woowahan.com");
            }
        };
    }

    @Test
    @DisplayName("비밀번호 변경과 이력 저장이 모두 성공하면 커밋하고, 롤백하지 않고, Connection을 닫는다")
    void commitsWhenAllSucceed() throws SQLException {
        final var userService = new UserService(userDao, new UserHistoryDao(new JdbcTemplate(mock(DataSource.class))), dataSource);

        userService.changePassword(USER_ID, NEW_PASSWORD, CREATE_BY);

        verify(connection).commit();
        verify(connection, never()).rollback();
        verify(connection).close();
    }

    @Test
    @DisplayName("autocommit을 끈 하나의 Connection에서 비밀번호 변경과 이력 저장을 실행한 뒤 커밋한다")
    void runsBothStatementsInOneTransaction() throws SQLException {
        final var userService = new UserService(userDao, new UserHistoryDao(new JdbcTemplate(mock(DataSource.class))), dataSource);

        userService.changePassword(USER_ID, NEW_PASSWORD, CREATE_BY);

        verify(dataSource, times(1)).getConnection();
        final var inOrder = inOrder(connection);
        inOrder.verify(connection).setAutoCommit(false);
        inOrder.verify(connection, times(2)).prepareStatement(anyString());
        inOrder.verify(connection).commit();
        inOrder.verify(connection).close();
    }

    @Test
    @DisplayName("이력 저장이 실패하면 롤백하고, 커밋하지 않고, Connection을 닫는다")
    void rollsBackWhenHistoryFails() throws SQLException {
        final var historyFailure = new DataAccessException("이력 저장 실패");
        final var userService = new UserService(userDao, failingHistoryDao(historyFailure), dataSource);

        assertThatThrownBy(() -> userService.changePassword(USER_ID, NEW_PASSWORD, CREATE_BY))
                .isInstanceOf(DataAccessException.class);

        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).close();
    }

    private UserHistoryDao failingHistoryDao(final RuntimeException failure) {
        return new UserHistoryDao(new JdbcTemplate(mock(DataSource.class))) {
            @Override
            public void log(final Connection connection, final UserHistory userHistory) {
                throw failure;
            }
        };
    }

    @Test
    @DisplayName("이력 저장이 실패하면 그 원래 예외를 잃지 않고 호출한 쪽에 전달한다")
    void keepsOriginalFailure() {
        final var historyFailure = new DataAccessException("이력 저장 실패");
        final var userService = new UserService(userDao, failingHistoryDao(historyFailure), dataSource);

        assertThatThrownBy(() -> userService.changePassword(USER_ID, NEW_PASSWORD, CREATE_BY))
                .isInstanceOf(DataAccessException.class)
                .satisfies(thrown -> assertThat(reachableThrowables(thrown)).contains(historyFailure));
    }

    // 원래 예외를 suppressed로 붙이든 cause로 감싸든 설계는 자유다. 두 예외가 모두 추적 가능하기만 하면 된다.
    @Test
    @DisplayName("롤백마저 실패해도 원래 예외와 롤백 실패를 모두 남기고, Connection은 닫는다")
    void keepsBothFailuresWhenRollbackFails() throws SQLException {
        final var historyFailure = new DataAccessException("이력 저장 실패");
        final var rollbackFailure = new SQLException("롤백 실패");
        willThrow(rollbackFailure).given(connection).rollback();
        final var userService = new UserService(userDao, failingHistoryDao(historyFailure), dataSource);

        assertThatThrownBy(() -> userService.changePassword(USER_ID, NEW_PASSWORD, CREATE_BY))
                .isInstanceOf(DataAccessException.class)
                .satisfies(thrown -> assertThat(reachableThrowables(thrown)).contains(historyFailure, rollbackFailure));

        verify(connection, never()).commit();
        verify(connection).close();
    }

    // 비밀번호 UPDATE가 실행된 뒤 DB와 무관한 이유로 실패하는 상황이다. 실패의 종류와 상관없이 앞선 UPDATE는 없던 일이 되어야 한다.
    @Test
    @DisplayName("DB와 무관한 RuntimeException으로 실패해도 롤백하고, 커밋하지 않고, 원래 예외를 전달한다")
    void rollsBackOnNonDataAccessRuntimeException() throws SQLException {
        final var businessFailure = new IllegalStateException("비즈니스 규칙 위반");
        final var userService = new UserService(userDao, failingHistoryDao(businessFailure), dataSource);

        assertThatThrownBy(() -> userService.changePassword(USER_ID, NEW_PASSWORD, CREATE_BY))
                .satisfies(thrown -> assertThat(reachableThrowables(thrown)).contains(businessFailure));

        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).close();
    }

    @Test
    @DisplayName("commit이 실패하면 롤백하고, 커밋 실패를 담은 DataAccessException을 던지고, Connection을 닫는다")
    void rollsBackWhenCommitFails() throws SQLException {
        final var commitFailure = new SQLException("커밋 실패");
        willThrow(commitFailure).given(connection).commit();
        final var userService = new UserService(userDao, new UserHistoryDao(new JdbcTemplate(mock(DataSource.class))), dataSource);

        assertThatThrownBy(() -> userService.changePassword(USER_ID, NEW_PASSWORD, CREATE_BY))
                .isInstanceOf(DataAccessException.class)
                .satisfies(thrown -> assertThat(reachableThrowables(thrown)).contains(commitFailure));

        final var inOrder = inOrder(connection);
        inOrder.verify(connection).commit();
        inOrder.verify(connection).rollback();
        inOrder.verify(connection).close();
    }

    @Test
    @DisplayName("Connection을 얻지 못하면 DataAccessException을 던지고, 쿼리를 실행하지 않는다")
    void failsWhenConnectionUnavailable() throws SQLException {
        given(dataSource.getConnection()).willThrow(new SQLException("연결 실패"));
        final var userService = new UserService(userDao, new UserHistoryDao(new JdbcTemplate(mock(DataSource.class))), dataSource);

        assertThatThrownBy(() -> userService.changePassword(USER_ID, NEW_PASSWORD, CREATE_BY))
                .isInstanceOf(DataAccessException.class)
                .hasRootCauseMessage("연결 실패");

        verify(connection, never()).prepareStatement(anyString());
    }
}
