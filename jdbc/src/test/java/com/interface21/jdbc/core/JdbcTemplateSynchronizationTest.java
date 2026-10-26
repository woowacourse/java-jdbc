package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JdbcTemplateSynchronizationTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        jdbcTemplate = new JdbcTemplate(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
    }

    @AfterEach
    void unbindTransaction() {
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Test
    @DisplayName("일반 API도 등록된 연결을 공유하고 SQL 자원만 닫는다")
    void allOrdinaryApisReuseTheBoundConnectionWithoutCompletingTheTransaction() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);
        when(resultSet.next()).thenReturn(true, false, true, false);
        when(resultSet.getString("name")).thenReturn("Alice");

        assertThat(jdbcTemplate.update("update sample set name = ?", "Alice")).isEqualTo(1);
        assertThat(jdbcTemplate.query("select name from sample", row -> row.getString("name")))
                .containsExactly("Alice");
        final var user = jdbcTemplate.queryForObject("select name from sample", row -> row.getString("name"));
        assertThat(user).isEqualTo("Alice");

        verify(statement, times(3)).close();
        verify(resultSet, times(2)).close();
        assertTransactionStillBound();
    }

    @Test
    @DisplayName("등록된 연결의 SQL 실패는 원인을 보존하고 연결과 등록을 유지한다")
    void sqlFailureKeepsTheBoundConnectionForTheTransactionOwner() throws SQLException {
        final var failure = new SQLException("update failed");
        doThrow(failure).when(statement).executeUpdate();

        final var thrown = assertThrows(DataAccessException.class,
                () -> jdbcTemplate.update("update sample set name = ?", "Alice"));

        assertThat(thrown.getCause()).isSameAs(failure);
        verify(statement).close();
        verifyNoInteractions(resultSet);
        assertTransactionStillBound();
    }

    @Test
    @DisplayName("등록된 연결의 단건 조회도 여러 행을 거부하고 트랜잭션 연결은 유지한다")
    void cardinalityFailurePreservesTheSharedConnectionAndReadsOnlyTwoRows() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, true);

        final var failure = assertThrows(DataAccessException.class,
                () -> jdbcTemplate.queryForObject("select name from sample", row -> row.getString("name")));

        assertThat(failure).hasMessage("Expected at most one row, but found two or more");
        verify(resultSet, times(2)).next();
        verify(resultSet).getString("name");
        verify(resultSet).close();
        verify(statement).close();
        assertTransactionStillBound();
    }

    @Test
    @DisplayName("등록 해제 후의 작업은 새 연결을 얻고 종료한다")
    void theNextOrdinaryOperationUsesAndClosesANewConnectionAfterUnbinding() throws SQLException {
        TransactionSynchronizationManager.unbindResource(dataSource);
        final var ordinaryConnection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(ordinaryConnection);
        when(ordinaryConnection.prepareStatement(anyString())).thenReturn(statement);

        jdbcTemplate.update("update sample set name = ?", "Alice");

        verify(dataSource).getConnection();
        verify(ordinaryConnection).close();
        verifyNoInteractions(connection);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
    }

    @Test
    @DisplayName("등록된 연결의 setter 실패 뒤 Statement 종료 실패도 원래 예외에 보존한다")
    void statementCleanupFailureDoesNotReplaceTheOriginalFailureInATransaction() throws SQLException {
        final var failure = new IllegalArgumentException("binding failed");
        final var closeFailure = new SQLException("statement close failed");
        doThrow(closeFailure).when(statement).close();

        final var thrown = assertThrows(IllegalArgumentException.class,
                () -> jdbcTemplate.update("update sample set name = ?", preparedStatement -> {
                    throw failure;
                }));

        assertThat(thrown).isSameAs(failure);
        assertThat(thrown.getSuppressed()).containsExactly(closeFailure);
        verify(statement, never()).executeUpdate();
        assertTransactionStillBound();
    }

    @Test
    @DisplayName("등록된 연결의 Statement 준비 실패에도 연결을 닫지 않는다")
    void preparationFailureLeavesTheTransactionConnectionBound() throws SQLException {
        final var failure = new SQLException("cannot prepare");
        when(connection.prepareStatement(anyString())).thenThrow(failure);

        final var thrown = assertThrows(DataAccessException.class,
                () -> jdbcTemplate.update("invalid sql"));

        assertThat(thrown.getCause()).isSameAs(failure);
        verifyNoInteractions(statement, resultSet);
        assertTransactionStillBound();
    }

    @Test
    @DisplayName("트랜잭션 연결이 등록돼 있어도 잘못된 매퍼는 연결 접근 전에 거부한다")
    void nullMapperIsRejectedWithoutTouchingTheBoundConnection() {
        assertThrows(NullPointerException.class,
                () -> jdbcTemplate.query("select name from sample", (RowMapper<String>) null));
        assertThrows(NullPointerException.class,
                () -> jdbcTemplate.queryForObject("select name from sample", (RowMapper<String>) null));

        verifyNoInteractions(dataSource, connection, statement, resultSet);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
    }

    private void assertTransactionStillBound() throws SQLException {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isSameAs(connection);
        verifyNoInteractions(dataSource);
        verify(connection, never()).close();
        verify(connection, never()).commit();
        verify(connection, never()).rollback();
    }
}
