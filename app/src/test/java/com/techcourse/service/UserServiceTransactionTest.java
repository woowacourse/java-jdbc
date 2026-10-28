package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.UserHistory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

// 실제 H2는 커밋하지 않은 Connection을 닫을 때 알아서 롤백해 준다.
// 그 동작은 JDBC 표준이 아니라 드라이버마다 다르므로, 여기서는 mock Connection으로 TxUserService가 직접 커밋, 롤백, 닫기를 하는지 확인한다.
// TransactionSynchronizationManager는 DataSource 인스턴스를 키로 쓰므로, TxUserService와 DAO는 같은 DataSource를 쓴다.
// DAO가 묶인 Connection을 찾지 못하고 따로 얻거나 닫으면, getConnection과 close의 호출 횟수로 드러난다.
class UserServiceTransactionTest {

    private static final long USER_ID = 1L;
    private static final String NEW_PASSWORD = "newPassword";
    private static final String CREATE_BY = "gugu";

    private DataSource dataSource;
    private Connection connection;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;
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
        resultSet = mock(ResultSet.class);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(anyString())).willReturn(preparedStatement);
        given(preparedStatement.executeUpdate()).willReturn(1);

        // findById가 조회할 때 돌려줄 사용자 한 명이다.
        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(true, false);
        given(resultSet.getLong("id")).willReturn(USER_ID);
        given(resultSet.getString("account")).willReturn("gugu");
        given(resultSet.getString("password")).willReturn("password");
        given(resultSet.getString("email")).willReturn("hkkang@woowahan.com");

        jdbcTemplate = new JdbcTemplate(dataSource);
        userDao = new UserDao(jdbcTemplate);
    }

    // 매니저의 상태는 static ThreadLocal이라, 구현이 바인딩을 남기더라도 같은 스레드의 다음 테스트로 넘기지 않는다.
    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private UserService txUserService(final UserHistoryDao userHistoryDao) {
        return new TxUserService(new AppUserService(userDao, userHistoryDao), dataSource);
    }

    private UserHistoryDao historyDao() {
        return new UserHistoryDao(jdbcTemplate);
    }

    private UserHistoryDao failingHistoryDao(final RuntimeException failure) {
        return new UserHistoryDao(jdbcTemplate) {
            @Override
            public void log(final UserHistory userHistory) {
                throw failure;
            }
        };
    }

    private void assertNoBoundConnection() {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("조회, 비밀번호 변경, 이력 저장을 서비스가 얻은 Connection 하나로 실행하고, DAO가 따로 Connection을 얻거나 닫지 않는다")
    void sharesServiceConnectionWithDao() throws SQLException {
        txUserService(historyDao()).changePassword(USER_ID, NEW_PASSWORD, CREATE_BY);

        verify(dataSource, times(1)).getConnection();
        verify(connection).prepareStatement(startsWith("select"));
        verify(connection).prepareStatement(startsWith("update"));
        verify(connection).prepareStatement(startsWith("insert"));
        verify(connection, times(1)).close();
    }

    @Test
    @DisplayName("비즈니스 로직이 도는 동안에는 서비스의 Connection이 묶여 있어서, DAO가 그것을 찾을 수 있다")
    void bindsConnectionWhileBusinessLogicRuns() {
        final List<Connection> boundWhileLogging = new ArrayList<>();
        final var observingHistoryDao = new UserHistoryDao(jdbcTemplate) {
            @Override
            public void log(final UserHistory userHistory) {
                boundWhileLogging.add(TransactionSynchronizationManager.getResource(dataSource));
                super.log(userHistory);
            }
        };

        txUserService(observingHistoryDao).changePassword(USER_ID, NEW_PASSWORD, CREATE_BY);

        assertThat(boundWhileLogging).containsExactly(connection);
    }

    @Test
    @DisplayName("비밀번호 변경과 이력 저장이 모두 성공하면 커밋하고, 롤백하지 않고, Connection을 닫고, 바인딩을 풀어둔다")
    void commitsWhenAllSucceed() throws SQLException {
        txUserService(historyDao()).changePassword(USER_ID, NEW_PASSWORD, CREATE_BY);

        verify(connection).commit();
        verify(connection, never()).rollback();
        verify(connection).close();
        assertNoBoundConnection();
    }

    @Test
    @DisplayName("autocommit을 끈 하나의 Connection에서 조회, 비밀번호 변경, 이력 저장을 실행한 뒤 커밋한다")
    void runsAllStatementsInOneTransaction() throws SQLException {
        txUserService(historyDao()).changePassword(USER_ID, NEW_PASSWORD, CREATE_BY);

        final var inOrder = inOrder(connection);
        inOrder.verify(connection).setAutoCommit(false);
        inOrder.verify(connection).prepareStatement(startsWith("select"));
        inOrder.verify(connection).prepareStatement(startsWith("update"));
        inOrder.verify(connection).prepareStatement(startsWith("insert"));
        inOrder.verify(connection).commit();
        inOrder.verify(connection).close();
    }

    // 톰캣은 스레드를 재사용한다. 앞 트랜잭션이 바인딩을 남기면 다음 트랜잭션의 bind가 중복 bind로 터진다.
    @Test
    @DisplayName("트랜잭션이 끝난 뒤 같은 스레드에서 다시 비밀번호를 바꿔도, 새 트랜잭션으로 정상 실행된다")
    void runsNextTransactionOnSameThread() throws SQLException {
        given(resultSet.next()).willReturn(true, false, true, false);
        final var userService = txUserService(historyDao());

        userService.changePassword(USER_ID, NEW_PASSWORD, CREATE_BY);
        userService.changePassword(USER_ID, "anotherPassword", CREATE_BY);

        verify(dataSource, times(2)).getConnection();
        verify(connection, times(2)).commit();
        assertNoBoundConnection();
    }

    @Test
    @DisplayName("이력 저장이 실패하면 롤백하고, 커밋하지 않고, Connection을 닫고, 바인딩을 풀어둔다")
    void rollsBackWhenHistoryFails() throws SQLException {
        final var historyFailure = new DataAccessException("이력 저장 실패");
        final var userService = txUserService(failingHistoryDao(historyFailure));

        assertThatThrownBy(() -> userService.changePassword(USER_ID, NEW_PASSWORD, CREATE_BY))
                .isInstanceOf(DataAccessException.class);

        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).close();
        assertNoBoundConnection();
    }

    @Test
    @DisplayName("이력 저장이 실패하면 그 원래 예외를 잃지 않고 호출한 쪽에 전달한다")
    void keepsOriginalFailure() {
        final var historyFailure = new DataAccessException("이력 저장 실패");
        final var userService = txUserService(failingHistoryDao(historyFailure));

        assertThatThrownBy(() -> userService.changePassword(USER_ID, NEW_PASSWORD, CREATE_BY))
                .isInstanceOf(DataAccessException.class)
                .satisfies(thrown -> assertThat(reachableThrowables(thrown)).contains(historyFailure));
    }

    // 원래 예외를 suppressed로 붙이든 cause로 감싸든 설계는 자유다. 두 예외가 모두 추적 가능하기만 하면 된다.
    @Test
    @DisplayName("롤백마저 실패해도 원래 예외와 롤백 실패를 모두 남기고, Connection은 닫고, 바인딩을 풀어둔다")
    void keepsBothFailuresWhenRollbackFails() throws SQLException {
        final var historyFailure = new DataAccessException("이력 저장 실패");
        final var rollbackFailure = new SQLException("롤백 실패");
        willThrow(rollbackFailure).given(connection).rollback();
        final var userService = txUserService(failingHistoryDao(historyFailure));

        assertThatThrownBy(() -> userService.changePassword(USER_ID, NEW_PASSWORD, CREATE_BY))
                .isInstanceOf(DataAccessException.class)
                .satisfies(thrown -> assertThat(reachableThrowables(thrown)).contains(historyFailure, rollbackFailure));

        verify(connection, never()).commit();
        verify(connection).close();
        assertNoBoundConnection();
    }

    // 비밀번호 UPDATE가 실행된 뒤 DB와 무관한 이유로 실패하는 상황이다. 실패의 종류와 상관없이 앞선 UPDATE는 없던 일이 되어야 한다.
    @Test
    @DisplayName("DB와 무관한 RuntimeException으로 실패해도 롤백하고, 커밋하지 않고, 원래 예외를 전달한다")
    void rollsBackOnNonDataAccessRuntimeException() throws SQLException {
        final var businessFailure = new IllegalStateException("비즈니스 규칙 위반");
        final var userService = txUserService(failingHistoryDao(businessFailure));

        assertThatThrownBy(() -> userService.changePassword(USER_ID, NEW_PASSWORD, CREATE_BY))
                .satisfies(thrown -> assertThat(reachableThrowables(thrown)).contains(businessFailure));

        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).close();
        assertNoBoundConnection();
    }

    @Test
    @DisplayName("commit이 실패하면 롤백하고, 커밋 실패를 담은 DataAccessException을 던지고, Connection을 닫고, 바인딩을 풀어둔다")
    void rollsBackWhenCommitFails() throws SQLException {
        final var commitFailure = new SQLException("커밋 실패");
        willThrow(commitFailure).given(connection).commit();
        final var userService = txUserService(historyDao());

        assertThatThrownBy(() -> userService.changePassword(USER_ID, NEW_PASSWORD, CREATE_BY))
                .isInstanceOf(DataAccessException.class)
                .satisfies(thrown -> assertThat(reachableThrowables(thrown)).contains(commitFailure));

        final var inOrder = inOrder(connection);
        inOrder.verify(connection).commit();
        inOrder.verify(connection).rollback();
        inOrder.verify(connection).close();
        assertNoBoundConnection();
    }

    @Test
    @DisplayName("Connection을 얻지 못하면 DataAccessException을 던지고, 쿼리를 실행하지 않고, 바인딩도 남기지 않는다")
    void failsWhenConnectionUnavailable() throws SQLException {
        given(dataSource.getConnection()).willThrow(new SQLException("연결 실패"));
        final var userService = txUserService(historyDao());

        assertThatThrownBy(() -> userService.changePassword(USER_ID, NEW_PASSWORD, CREATE_BY))
                .isInstanceOf(DataAccessException.class)
                .hasRootCauseMessage("연결 실패");

        verify(connection, never()).prepareStatement(anyString());
        assertNoBoundConnection();
    }

    // 트랜잭션을 시작하다 실패하면 아직 아무것도 묶이지 않은 상태다. 정리하다가 난 다른 예외가 원래 실패를 가리면 안 된다.
    @Test
    @DisplayName("autocommit을 끄다 실패하면 그 원인을 담은 DataAccessException을 던지고, 쿼리를 실행하지 않고, Connection은 닫는다")
    void failsWhenSetAutoCommitFails() throws SQLException {
        willThrow(new SQLException("autocommit 실패")).given(connection).setAutoCommit(false);
        final var userService = txUserService(historyDao());

        assertThatThrownBy(() -> userService.changePassword(USER_ID, NEW_PASSWORD, CREATE_BY))
                .isInstanceOf(DataAccessException.class)
                .hasRootCauseMessage("autocommit 실패");

        verify(connection, never()).prepareStatement(anyString());
        verify(connection).close();
        assertNoBoundConnection();
    }
}
