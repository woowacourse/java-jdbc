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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
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

    private static List<Throwable> causeChain(final Throwable root) {
        final List<Throwable> chain = new ArrayList<>();
        for (Throwable current = root; current != null && !chain.contains(current); current = current.getCause()) {
            chain.add(current);
        }
        return chain;
    }

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        given(dataSource.getConnection()).willReturn(connection);
        // JDBC 기본값처럼, 새로 얻은 Connection은 autocommit이 켜져 있다.
        given(connection.getAutoCommit()).willReturn(true);
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

    @Test
    @DisplayName("성공하면 커밋한 뒤 autocommit을 되돌리고 나서 Connection을 닫는다")
    void restoresAutoCommitAfterCommit() throws SQLException {
        txUserService(historyDao()).changePassword(USER_ID, NEW_PASSWORD, CREATE_BY);

        final var inOrder = inOrder(connection);
        inOrder.verify(connection).commit();
        inOrder.verify(connection).setAutoCommit(true);
        inOrder.verify(connection).close();
    }

    @Test
    @DisplayName("업무 로직이 실패해 롤백해도, autocommit을 되돌리고 나서 Connection을 닫는다")
    void restoresAutoCommitAfterRollback() throws SQLException {
        final var userService = txUserService(failingHistoryDao(new DataAccessException("이력 저장 실패")));

        assertThatThrownBy(() -> userService.changePassword(USER_ID, NEW_PASSWORD, CREATE_BY))
                .isInstanceOf(DataAccessException.class);

        final var inOrder = inOrder(connection);
        inOrder.verify(connection).rollback();
        inOrder.verify(connection).setAutoCommit(true);
        inOrder.verify(connection).close();
    }

    @Test
    @DisplayName("커밋이 실패해 롤백해도, autocommit을 되돌리고 나서 Connection을 닫는다")
    void restoresAutoCommitAfterCommitFailure() throws SQLException {
        willThrow(new SQLException("커밋 실패")).given(connection).commit();
        final var userService = txUserService(historyDao());

        assertThatThrownBy(() -> userService.changePassword(USER_ID, NEW_PASSWORD, CREATE_BY))
                .isInstanceOf(DataAccessException.class);

        final var inOrder = inOrder(connection);
        inOrder.verify(connection).rollback();
        inOrder.verify(connection).setAutoCommit(true);
        inOrder.verify(connection).close();
    }

    @Test
    @DisplayName("커밋한 뒤 autocommit 복구가 실패해도 예외를 던지지 않고, 롤백하지 않고, Connection은 닫고 바인딩을 풀어둔다")
    void doesNotFailWhenRestoreFailsAfterCommit() throws SQLException {
        willThrow(new SQLException("복구 실패")).given(connection).setAutoCommit(true);
        final var userService = txUserService(historyDao());

        assertThatCode(() -> userService.changePassword(USER_ID, NEW_PASSWORD, CREATE_BY))
                .doesNotThrowAnyException();

        verify(connection).commit();
        verify(connection, never()).rollback();
        verify(connection).close();
        assertNoBoundConnection();
    }

    @Test
    @DisplayName("업무 로직이 실패한 뒤 autocommit 복구까지 실패해도, 원래 예외를 주 원인으로 던지고 Connection은 닫고 바인딩을 풀어둔다")
    void keepsOriginalFailureWhenRestoreFails() throws SQLException {
        final var historyFailure = new DataAccessException("이력 저장 실패");
        final var restoreFailure = new SQLException("복구 실패");
        willThrow(restoreFailure).given(connection).setAutoCommit(true);
        final var userService = txUserService(failingHistoryDao(historyFailure));

        assertThatThrownBy(() -> userService.changePassword(USER_ID, NEW_PASSWORD, CREATE_BY))
                .satisfies(thrown -> {
                    assertThat(causeChain(thrown)).contains(historyFailure);
                    assertThat(causeChain(thrown)).doesNotContain(restoreFailure);
                });

        verify(connection).setAutoCommit(true);
        verify(connection).close();
        assertNoBoundConnection();
    }

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

    @Test
    @DisplayName("업무 로직이 실패한 뒤 롤백마저 실패하면, autocommit을 되돌리지 않고 Connection을 닫고 바인딩을 풀어둔다")
    void doesNotRestoreAutoCommitWhenRollbackFails() throws SQLException {
        willThrow(new SQLException("롤백 실패")).given(connection).rollback();
        final var userService = txUserService(failingHistoryDao(new DataAccessException("이력 저장 실패")));

        assertThatThrownBy(() -> userService.changePassword(USER_ID, NEW_PASSWORD, CREATE_BY))
                .isInstanceOf(DataAccessException.class);

        verify(connection, never()).setAutoCommit(true);
        verify(connection).close();
        assertNoBoundConnection();
    }

    @Test
    @DisplayName("커밋이 실패한 뒤 롤백마저 실패하면, autocommit을 되돌리지 않고 Connection을 닫고 바인딩을 풀어둔다")
    void doesNotRestoreAutoCommitWhenRollbackFailsAfterCommitFailure() throws SQLException {
        willThrow(new SQLException("커밋 실패")).given(connection).commit();
        willThrow(new SQLException("롤백 실패")).given(connection).rollback();
        final var userService = txUserService(historyDao());

        assertThatThrownBy(() -> userService.changePassword(USER_ID, NEW_PASSWORD, CREATE_BY))
                .isInstanceOf(DataAccessException.class);

        verify(connection, never()).setAutoCommit(true);
        verify(connection).close();
        assertNoBoundConnection();
    }

    @Test
    @DisplayName("바깥 트랜잭션이 있으면 그 Connection으로 실행하고, DataSource에서 새 Connection을 얻지 않는다")
    void participatesInOuterTransactionWithItsConnection() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        txUserService(historyDao()).changePassword(USER_ID, NEW_PASSWORD, CREATE_BY);

        verify(dataSource, never()).getConnection();
        verify(connection).prepareStatement(startsWith("update"));
        verify(connection).prepareStatement(startsWith("insert"));
    }

    @Test
    @DisplayName("바깥 트랜잭션에 참여하면 autocommit 변경, 커밋, 롤백, 닫기를 하지 않고, 바깥의 바인딩도 그대로 둔다")
    void leavesOuterTransactionUntouchedWhenParticipating() throws SQLException {
        TransactionSynchronizationManager.bindResource(dataSource, connection);

        txUserService(historyDao()).changePassword(USER_ID, NEW_PASSWORD, CREATE_BY);

        verify(connection, never()).setAutoCommit(anyBoolean());
        verify(connection, never()).commit();
        verify(connection, never()).rollback();
        verify(connection, never()).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    @Test
    @DisplayName("바깥 트랜잭션에 참여하다 실패하면 롤백하거나 닫지 않고, 원래 예외를 그대로 전달하고, 바깥의 바인딩도 그대로 둔다")
    void propagatesFailureToOuterTransactionWithoutRollingBack() throws SQLException {
        final var historyFailure = new DataAccessException("이력 저장 실패");
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        final var userService = txUserService(failingHistoryDao(historyFailure));

        assertThatThrownBy(() -> userService.changePassword(USER_ID, NEW_PASSWORD, CREATE_BY))
                .satisfies(thrown -> assertThat(causeChain(thrown)).contains(historyFailure));

        verify(connection, never()).rollback();
        verify(connection, never()).close();
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }
}
